import React, { useState, useEffect, useCallback } from 'react';
import { User } from 'firebase/auth';
import { 
  initAuth, 
  googleSignIn, 
  logout, 
  getAccessToken 
} from './services/firebase';
import { 
  getSpreadsheetMeta, 
  getSheetValues, 
  parseSheetRowsToTransactions, 
  appendTransactionToSheet, 
  DEFAULT_SPREADSHEET_ID, 
  extractSpreadsheetId 
} from './services/sheetsApi';
import { 
  Account, 
  Category, 
  CreditCard, 
  FinancialGoal, 
  GoogleSheetMeta, 
  Transaction 
} from './types';
import { 
  INITIAL_CATEGORIES, 
  INITIAL_ACCOUNTS, 
  INITIAL_CREDIT_CARDS, 
  INITIAL_GOALS, 
  INITIAL_TRANSACTIONS,
  MONTH_NAMES 
} from './data/initialData';

import { Header } from './components/Header';
import { DashboardView } from './components/DashboardView';
import { TransactionsView } from './components/TransactionsView';
import { BudgetPlannerView } from './components/BudgetPlannerView';
import { AccountsAndCardsView } from './components/AccountsAndCardsView';
import { GoalsView } from './components/GoalsView';
import { ReportsView } from './components/ReportsView';
import { TransactionModal } from './components/TransactionModal';
import { GoogleSyncModal } from './components/GoogleSyncModal';
import { ConfirmationModal } from './components/ConfirmationModal';
import { CategoryModal } from './components/CategoryModal';
import { InstallAppModal } from './components/InstallAppModal';

export default function App() {
  const [currentTab, setCurrentTab] = useState<string>('dashboard');
  const [selectedMonth, setSelectedMonth] = useState<number>(7); // Agosto (0-indexed 7)
  const [selectedYear, setSelectedYear] = useState<number>(2026);

  // User state & Auth
  const [user, setUser] = useState<User | null>(null);
  const [isLoggingIn, setIsLoggingIn] = useState(false);

  // Google Sheets integration state
  const [sheetMeta, setSheetMeta] = useState<GoogleSheetMeta | null>(null);
  const [spreadsheetInput, setSpreadsheetInput] = useState<string>(DEFAULT_SPREADSHEET_ID);
  const [isSyncing, setIsSyncing] = useState<boolean>(false);
  const [syncMessage, setSyncMessage] = useState<string | null>(null);
  const [syncError, setSyncError] = useState<string | null>(null);

  // Modals state
  const [isSyncModalOpen, setIsSyncModalOpen] = useState(false);
  const [isTransactionModalOpen, setIsTransactionModalOpen] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [categoryModalDefaultType, setCategoryModalDefaultType] = useState<'expense' | 'income'>('expense');
  const [isInstallModalOpen, setIsInstallModalOpen] = useState(false);

  // Confirmation dialog state (mandatory for workspace mutations / deletions)
  const [confirmModalState, setConfirmModalState] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    confirmLabel?: string;
    cancelLabel?: string;
    secondaryLabel?: string;
    isDestructive?: boolean;
    onConfirm: () => void;
    onCancel?: () => void;
    onSecondaryAction?: () => void;
  }>({
    isOpen: false,
    title: '',
    message: '',
    isDestructive: false,
    onConfirm: () => {}
  });

  // Financial Data State (with localStorage persistence)
  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    const saved = localStorage.getItem('fin_transactions_2026');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { console.error(e); }
    }
    return INITIAL_TRANSACTIONS;
  });

  const [categories, setCategories] = useState<Category[]>(() => {
    const saved = localStorage.getItem('fin_categories_2026');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { console.error(e); }
    }
    return INITIAL_CATEGORIES;
  });

  const [accounts, setAccounts] = useState<Account[]>(() => {
    const saved = localStorage.getItem('fin_accounts_2026');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { console.error(e); }
    }
    return INITIAL_ACCOUNTS;
  });

  const [creditCards, setCreditCards] = useState<CreditCard[]>(() => {
    const saved = localStorage.getItem('fin_cards_2026');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { console.error(e); }
    }
    return INITIAL_CREDIT_CARDS;
  });

  const [goals, setGoals] = useState<FinancialGoal[]>(() => {
    const saved = localStorage.getItem('fin_goals_2026');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) { console.error(e); }
    }
    return INITIAL_GOALS;
  });

  // Save to localStorage whenever state changes
  useEffect(() => {
    localStorage.setItem('fin_transactions_2026', JSON.stringify(transactions));
  }, [transactions]);

  useEffect(() => {
    localStorage.setItem('fin_categories_2026', JSON.stringify(categories));
  }, [categories]);

  useEffect(() => {
    localStorage.setItem('fin_accounts_2026', JSON.stringify(accounts));
  }, [accounts]);

  useEffect(() => {
    localStorage.setItem('fin_cards_2026', JSON.stringify(creditCards));
  }, [creditCards]);

  useEffect(() => {
    localStorage.setItem('fin_goals_2026', JSON.stringify(goals));
  }, [goals]);

  // Handle Sheet Connection & Initial Fetch
  const loadSheetInfo = useCallback(async (token: string, sheetId: string) => {
    try {
      setIsSyncing(true);
      setSyncError(null);
      const meta = await getSpreadsheetMeta(token, sheetId);
      setSheetMeta(meta);
      setSyncMessage(`Planilha "${meta.title}" vinculada com sucesso!`);
    } catch (err: any) {
      console.error('Erro ao conectar planilha:', err);
      setSyncError(err.message || 'Não foi possível carregar as informações da planilha.');
    } finally {
      setIsSyncing(false);
    }
  }, []);

  // Sync Google Auth listener
  useEffect(() => {
    const unsubscribe = initAuth(
      async (authenticatedUser, token) => {
        setUser(authenticatedUser);
        const targetId = extractSpreadsheetId(spreadsheetInput);
        if (targetId && token) {
          loadSheetInfo(token, targetId);
        }
      },
      () => {
        setUser(null);
        setSheetMeta(null);
      }
    );

    return () => unsubscribe();
  }, [spreadsheetInput, loadSheetInfo]);

  // Google Sign In action
  const handleGoogleLogin = async () => {
    setIsLoggingIn(true);
    setSyncError(null);
    try {
      const res = await googleSignIn();
      if (res) {
        setUser(res.user);
        const targetId = extractSpreadsheetId(spreadsheetInput);
        await loadSheetInfo(res.accessToken, targetId);
      }
    } catch (err: any) {
      console.error('Login error:', err);
      setSyncError(err.message || 'Falha ao autenticar com o Google.');
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    setUser(null);
    setSheetMeta(null);
    setSyncMessage(null);
  };

  // Sync Data from Google Sheets
  const handleSyncFromSheets = async () => {
    const token = await getAccessToken();
    if (!token) {
      setSyncError('Faça login com sua conta do Google para sincronizar.');
      return;
    }

    const sheetId = sheetMeta?.spreadsheetId || extractSpreadsheetId(spreadsheetInput);
    setIsSyncing(true);
    setSyncError(null);
    setSyncMessage(null);

    try {
      // Find the first sheet tab or sheet titled "Lançamentos" / "Receitas e Despesas" / "Extrato"
      const meta = sheetMeta || await getSpreadsheetMeta(token, sheetId);
      const sheetTabs = meta.sheets || [];
      const primaryTab = sheetTabs.find(s => 
        s.title.toLowerCase().includes('lança') || 
        s.title.toLowerCase().includes('extrato') || 
        s.title.toLowerCase().includes('despesa') ||
        s.title.toLowerCase().includes('movimenta')
      )?.title || sheetTabs[0]?.title || 'Sheet1';

      // Read values from tab
      const rows = await getSheetValues(token, sheetId, `${primaryTab}!A1:H200`);
      const parsedTransactions = parseSheetRowsToTransactions(rows);

      if (parsedTransactions.length > 0) {
        // Merge with existing avoiding duplicates
        setTransactions(prev => {
          const newEntries = parsedTransactions.filter(
            pt => !prev.some(et => et.date === pt.date && et.description === pt.description && Math.abs(et.amount - pt.amount) < 0.01)
          );
          return [...newEntries, ...prev];
        });
        setSyncMessage(`Importação concluída! ${parsedTransactions.length} lançamentos sincronizados da aba "${primaryTab}".`);
      } else {
        setSyncMessage(`Nenhum lançamento tabulado encontrado na aba "${primaryTab}". Estrutura de dados pronta.`);
      }
    } catch (err: any) {
      console.error('Erro de sincronização:', err);
      setSyncError(err.message || 'Erro ao sincronizar com Google Sheets.');
    } finally {
      setIsSyncing(false);
    }
  };

  // Export Local Transactions to Google Sheets (with explicit user confirmation!)
  const handleExportLocalToSheet = () => {
    setConfirmModalState({
      isOpen: true,
      title: 'Exportar Lançamentos para a Planilha Google',
      message: `Você deseja enviar ${transactions.length} lançamentos cadastrados neste aplicativo diretamente para a sua planilha "${sheetMeta?.title || 'Controle Financeiro'}"? Novas linhas serão adicionadas ao final da aba correspondente.`,
      isDestructive: false,
      onConfirm: async () => {
        setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        const token = await getAccessToken();
        if (!token) {
          setSyncError('Token do Google não disponível.');
          return;
        }

        const sheetId = sheetMeta?.spreadsheetId || extractSpreadsheetId(spreadsheetInput);
        const sheetTab = sheetMeta?.sheets[0]?.title || 'Lançamentos';

        setIsSyncing(true);
        try {
          let exported = 0;
          for (const tx of transactions.slice(0, 10)) { // export batch safely
            await appendTransactionToSheet(token, sheetId, sheetTab, tx);
            exported++;
          }
          setSyncMessage(`${exported} lançamentos foram gravados com sucesso na sua Planilha Google!`);
        } catch (err: any) {
          setSyncError(err.message || 'Erro ao salvar lançamentos na planilha.');
        } finally {
          setIsSyncing(false);
        }
      }
    });
  };

  // Transaction CRUD Operations
  const handleSaveTransaction = async (
    txList: (Omit<Transaction, 'id'> & { id?: string })[],
    saveToSheet: boolean
  ) => {
    if (txList.length === 1 && txList[0].id) {
      // Editing existing single transaction
      const single = txList[0];
      setTransactions(prev =>
        prev.map(t => (t.id === single.id ? { ...single, id: single.id! } : t))
      );
    } else {
      // Creating one or multiple new transactions (e.g. installments or recurring)
      const newTransactions: Transaction[] = txList.map((tx, idx) => ({
        ...tx,
        id: tx.id || `tx-${Date.now()}-${idx}-${Math.random().toString(36).substr(2, 4)}`
      }));

      setTransactions(prev => [...newTransactions, ...prev]);

      // If requested to sync directly to connected Google Sheet
      if (saveToSheet && sheetMeta) {
        const token = await getAccessToken();
        if (token) {
          const sheetTab = sheetMeta.sheets[0]?.title || 'Lançamentos';
          try {
            for (const tx of newTransactions) {
              await appendTransactionToSheet(token, sheetMeta.spreadsheetId, sheetTab, tx);
            }
            setSyncMessage(`${newTransactions.length} lançamento(s) registrado(s) no app e na planilha!`);
          } catch (err) {
            console.error('Erro ao gravar linha na planilha:', err);
          }
        }
      }
    }
    setEditingTransaction(null);
  };

  const handleToggleStatus = (id: string) => {
    setTransactions(prev =>
      prev.map(t =>
        t.id === id ? { ...t, status: t.status === 'completed' ? 'pending' : 'completed' } : t
      )
    );
  };

  const handleRequestDeleteTransaction = (t: Transaction) => {
    if (t.installmentGroupId) {
      const allGroup = transactions.filter(item => item.installmentGroupId === t.installmentGroupId);
      setConfirmModalState({
        isOpen: true,
        title: 'Excluir Parcela / Compra Parcelada',
        message: `Este lançamento é a parcela ${t.installmentCurrent}/${t.installmentTotal} de "${t.description}". Deseja excluir todas as ${allGroup.length} parcelas vinculadas a esta compra ou apenas esta parcela individual?`,
        isDestructive: true,
        confirmLabel: `Excluir Todas (${allGroup.length}x)`,
        secondaryLabel: 'Excluir Apenas Esta',
        cancelLabel: 'Cancelar',
        onConfirm: () => {
          setTransactions(prev => prev.filter(item => item.installmentGroupId !== t.installmentGroupId));
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        },
        onSecondaryAction: () => {
          setTransactions(prev => prev.filter(item => item.id !== t.id));
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        },
        onCancel: () => {
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        }
      });
    } else if (t.recurrenceGroupId) {
      const allGroup = transactions.filter(item => item.recurrenceGroupId === t.recurrenceGroupId);
      setConfirmModalState({
        isOpen: true,
        title: 'Excluir Lançamento Fixo / Recorrente',
        message: `Este lançamento faz parte de uma despesa/receita fixa recorrente ("${t.description}"). Deseja excluir todos os ${allGroup.length} lançamentos fixos ou apenas este mês?`,
        isDestructive: true,
        confirmLabel: `Excluir Todos (${allGroup.length} meses)`,
        secondaryLabel: 'Excluir Apenas Este Mês',
        cancelLabel: 'Cancelar',
        onConfirm: () => {
          setTransactions(prev => prev.filter(item => item.recurrenceGroupId !== t.recurrenceGroupId));
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        },
        onSecondaryAction: () => {
          setTransactions(prev => prev.filter(item => item.id !== t.id));
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        },
        onCancel: () => {
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        }
      });
    } else {
      setConfirmModalState({
        isOpen: true,
        title: 'Excluir Lançamento',
        message: `Tem certeza que deseja excluir o lançamento "${t.description}" no valor de R$ ${t.amount.toFixed(2)}? Esta ação não poderá ser desfeita.`,
        isDestructive: true,
        confirmLabel: 'Excluir Lançamento',
        cancelLabel: 'Cancelar',
        onConfirm: () => {
          setTransactions(prev => prev.filter(item => item.id !== t.id));
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        },
        onCancel: () => {
          setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        }
      });
    }
  };

  const handleResetMonthData = () => {
    const monthPrefix = `${selectedYear}-${String(selectedMonth + 1).padStart(2, '0')}`;
    const monthTransactions = transactions.filter(t => t.date.startsWith(monthPrefix));
    const monthName = MONTH_NAMES[selectedMonth];

    setConfirmModalState({
      isOpen: true,
      title: `Resetar Informações de ${monthName} ${selectedYear}`,
      message: `Tem certeza que deseja resetar os dados de ${monthName} de ${selectedYear}? Isso excluirá todos os ${monthTransactions.length} lançamentos cadastrados deste período. Esta ação não poderá ser desfeita.`,
      isDestructive: true,
      onConfirm: () => {
        setTransactions(prev => prev.filter(t => !t.date.startsWith(monthPrefix)));
        setConfirmModalState(prev => ({ ...prev, isOpen: false }));
      }
    });
  };

  const handleUpdateCategoryBudget = (categoryId: string, newBudget: number) => {
    setCategories(prev =>
      prev.map(c => (c.id === categoryId ? { ...c, monthlyBudget: newBudget } : c))
    );
  };

  const handleOpenAddCategory = (defaultType: 'expense' | 'income' = 'expense') => {
    setEditingCategory(null);
    setCategoryModalDefaultType(defaultType);
    setIsCategoryModalOpen(true);
  };

  const handleOpenEditCategory = (category: Category) => {
    setEditingCategory(category);
    setCategoryModalDefaultType(category.type);
    setIsCategoryModalOpen(true);
  };

  const handleSaveCategory = (
    categoryData: Omit<Category, 'id'>,
    existingId?: string,
    oldName?: string
  ) => {
    if (existingId) {
      // Update existing category
      setCategories(prev =>
        prev.map(c => (c.id === existingId ? { ...categoryData, id: existingId } : c))
      );

      // If category name changed, update past transactions to keep them linked
      if (oldName && oldName !== categoryData.name) {
        setTransactions(prev =>
          prev.map(t => (t.category === oldName ? { ...t, category: categoryData.name } : t))
        );
      }

      setSyncMessage(`Categoria "${categoryData.name}" atualizada com sucesso!`);
    } else {
      // Create new category
      const newCategory: Category = {
        ...categoryData,
        id: `cat-${Date.now()}`
      };
      setCategories(prev => [...prev, newCategory]);
      setSyncMessage(`Categoria "${categoryData.name}" criada com sucesso!`);
    }
  };

  const handleDeleteCategory = (category: Category) => {
    const usageCount = transactions.filter(t => t.category === category.name).length;
    const warningText = usageCount > 0 
      ? `Atenção: Existem ${usageCount} lançamento(s) vinculados a esta categoria. Ao excluí-la, esses lançamentos manterão o histórico, mas a categoria não estará mais disponível para novos lançamentos.`
      : `Tem certeza que deseja excluir a categoria "${category.name}"?`;

    setConfirmModalState({
      isOpen: true,
      title: `Excluir Categoria "${category.name}"`,
      message: warningText,
      isDestructive: true,
      onConfirm: () => {
        setCategories(prev => prev.filter(c => c.id !== category.id));
        setConfirmModalState(prev => ({ ...prev, isOpen: false }));
        setSyncMessage(`Categoria "${category.name}" removida com sucesso.`);
      }
    });
  };

  const handleUpdateAccountBalance = (accountId: string, newBalance: number) => {
    setAccounts(prev =>
      prev.map(a => (a.id === accountId ? { ...a, balance: newBalance } : a))
    );
  };

  const handleAddAccount = (newAccountData: Omit<Account, 'id'>) => {
    const newAcc: Account = {
      ...newAccountData,
      id: `acc-${Date.now()}`
    };
    setAccounts(prev => [...prev, newAcc]);
  };

  const handleEditAccount = (updatedAccount: Account) => {
    setAccounts(prev => prev.map(a => (a.id === updatedAccount.id ? updatedAccount : a)));
  };

  const handleDeleteAccount = (account: Account) => {
    setConfirmModalState({
      isOpen: true,
      title: 'Excluir Conta Bancária',
      message: `Tem certeza que deseja excluir a conta "${account.name}" (${account.institution})?`,
      isDestructive: true,
      onConfirm: () => {
        setAccounts(prev => prev.filter(a => a.id !== account.id));
        setConfirmModalState(prev => ({ ...prev, isOpen: false }));
      }
    });
  };

  const handleAddCreditCard = (newCardData: Omit<CreditCard, 'id'>) => {
    const newCard: CreditCard = {
      ...newCardData,
      id: `card-${Date.now()}`
    };
    setCreditCards(prev => [...prev, newCard]);
  };

  const handleEditCreditCard = (updatedCard: CreditCard) => {
    setCreditCards(prev => prev.map(c => (c.id === updatedCard.id ? updatedCard : c)));
  };

  const handleDeleteCreditCard = (card: CreditCard) => {
    setConfirmModalState({
      isOpen: true,
      title: 'Excluir Cartão de Crédito',
      message: `Tem certeza que deseja excluir o cartão "${card.name}" (${card.institution})?`,
      isDestructive: true,
      onConfirm: () => {
        setCreditCards(prev => prev.filter(c => c.id !== card.id));
        setConfirmModalState(prev => ({ ...prev, isOpen: false }));
      }
    });
  };

  const handlePayCardInvoice = (
    card: CreditCard,
    sourceAccountName: string,
    amount: number,
    paymentDate: string
  ) => {
    // 1. Deduct amount from bank account
    setAccounts(prev =>
      prev.map(acc =>
        acc.name === sourceAccountName
          ? { ...acc, balance: acc.balance - amount }
          : acc
      )
    );

    // 2. Record transaction for bill payment
    const paymentTx: Transaction = {
      id: `tx-paycard-${Date.now()}`,
      description: `Pagamento Fatura ${card.name}`,
      amount: amount,
      type: 'expense',
      category: 'Contas & Assinaturas',
      account: sourceAccountName,
      date: paymentDate || new Date().toISOString().split('T')[0],
      status: 'completed',
      notes: `Pagamento da fatura do cartão ${card.institution}`
    };

    setTransactions(prev => [paymentTx, ...prev]);

    // 3. Reset or decrease currentInvoice
    setCreditCards(prev =>
      prev.map(c =>
        c.id === card.id
          ? { ...c, currentInvoice: Math.max(0, c.currentInvoice - amount) }
          : c
      )
    );

    setSyncMessage(`Fatura do cartão "${card.name}" de R$ ${amount.toFixed(2)} liquidada com sucesso!`);
  };

  const handleAddGoal = (newGoalData: Omit<FinancialGoal, 'id'>) => {
    const goal: FinancialGoal = {
      ...newGoalData,
      id: `goal-${Date.now()}`
    };
    setGoals(prev => [goal, ...prev]);
  };

  const handleUpdateGoalAmount = (goalId: string, addedAmount: number) => {
    setGoals(prev =>
      prev.map(g => (g.id === goalId ? { ...g, currentAmount: g.currentAmount + addedAmount } : g))
    );
  };

  const handleDeleteGoal = (goal: FinancialGoal) => {
    setConfirmModalState({
      isOpen: true,
      title: 'Excluir Meta Financeira',
      message: `Deseja remover a meta "${goal.title}"?`,
      isDestructive: true,
      onConfirm: () => {
        setGoals(prev => prev.filter(g => g.id !== goal.id));
        setConfirmModalState(prev => ({ ...prev, isOpen: false }));
      }
    });
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 flex flex-col font-sans antialiased transition-colors">
      {/* Header with Navigation & Sync Status */}
      <Header
        currentTab={currentTab}
        setCurrentTab={setCurrentTab}
        selectedMonth={selectedMonth}
        setSelectedMonth={setSelectedMonth}
        selectedYear={selectedYear}
        user={user}
        sheetMeta={sheetMeta}
        isSyncing={isSyncing}
        onOpenSyncModal={() => setIsSyncModalOpen(true)}
        onOpenNewTransaction={() => {
          setEditingTransaction(null);
          setIsTransactionModalOpen(true);
        }}
        onResetMonth={handleResetMonthData}
        onOpenInstallModal={() => setIsInstallModalOpen(true)}
        onLogin={handleGoogleLogin}
        onLogout={handleLogout}
        isLoggingIn={isLoggingIn}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 pt-6">
        {currentTab === 'dashboard' && (
          <DashboardView
            selectedMonth={selectedMonth}
            selectedYear={selectedYear}
            transactions={transactions}
            categories={categories}
            accounts={accounts}
            creditCards={creditCards}
            goals={goals}
            onOpenNewTransaction={() => {
              setEditingTransaction(null);
              setIsTransactionModalOpen(true);
            }}
            onOpenTransactionsTab={() => setCurrentTab('transactions')}
            onToggleTransactionStatus={handleToggleStatus}
          />
        )}

        {currentTab === 'transactions' && (
          <TransactionsView
            transactions={transactions}
            categories={categories}
            accounts={accounts}
            creditCards={creditCards}
            selectedMonth={selectedMonth}
            selectedYear={selectedYear}
            onOpenNewTransaction={() => {
              setEditingTransaction(null);
              setIsTransactionModalOpen(true);
            }}
            onEditTransaction={(t) => {
              setEditingTransaction(t);
              setIsTransactionModalOpen(true);
            }}
            onRequestDeleteTransaction={handleRequestDeleteTransaction}
            onToggleStatus={handleToggleStatus}
            onResetMonth={handleResetMonthData}
          />
        )}

        {currentTab === 'budget' && (
          <BudgetPlannerView
            selectedMonth={selectedMonth}
            selectedYear={selectedYear}
            categories={categories}
            transactions={transactions}
            onUpdateCategoryBudget={handleUpdateCategoryBudget}
            onOpenAddCategory={handleOpenAddCategory}
            onOpenEditCategory={handleOpenEditCategory}
            onDeleteCategory={handleDeleteCategory}
          />
        )}

        {currentTab === 'accounts' && (
          <AccountsAndCardsView
            accounts={accounts}
            creditCards={creditCards}
            transactions={transactions}
            selectedMonth={selectedMonth}
            selectedYear={selectedYear}
            onOpenNewTransaction={() => {
              setEditingTransaction(null);
              setIsTransactionModalOpen(true);
            }}
            onUpdateAccountBalance={handleUpdateAccountBalance}
            onAddAccount={handleAddAccount}
            onEditAccount={handleEditAccount}
            onDeleteAccount={handleDeleteAccount}
            onAddCreditCard={handleAddCreditCard}
            onEditCreditCard={handleEditCreditCard}
            onDeleteCreditCard={handleDeleteCreditCard}
            onPayCardInvoice={handlePayCardInvoice}
          />
        )}

        {currentTab === 'goals' && (
          <GoalsView
            goals={goals}
            onAddGoal={handleAddGoal}
            onUpdateGoalAmount={handleUpdateGoalAmount}
            onDeleteGoal={handleDeleteGoal}
          />
        )}

        {currentTab === 'reports' && (
          <ReportsView
            transactions={transactions}
            categories={categories}
            selectedYear={selectedYear}
          />
        )}
      </main>

      {/* Transaction Add / Edit Modal */}
      <TransactionModal
        isOpen={isTransactionModalOpen}
        onClose={() => {
          setIsTransactionModalOpen(false);
          setEditingTransaction(null);
        }}
        onSave={handleSaveTransaction}
        initialTransaction={editingTransaction}
        categories={categories}
        accounts={accounts}
        creditCards={creditCards}
        isGoogleConnected={!!sheetMeta}
        onOpenCreateCategory={handleOpenAddCategory}
      />

      {/* Category Creation / Edition Modal */}
      <CategoryModal
        isOpen={isCategoryModalOpen}
        onClose={() => {
          setIsCategoryModalOpen(false);
          setEditingCategory(null);
        }}
        onSave={handleSaveCategory}
        initialCategory={editingCategory}
        defaultType={categoryModalDefaultType}
      />

      {/* Install Mobile App / APK Instructions Modal */}
      <InstallAppModal
        isOpen={isInstallModalOpen}
        onClose={() => setIsInstallModalOpen(false)}
      />

      {/* Google Sheets Sync Manager Modal */}
      <GoogleSyncModal
        isOpen={isSyncModalOpen}
        onClose={() => setIsSyncModalOpen(false)}
        user={user}
        sheetMeta={sheetMeta}
        spreadsheetInput={spreadsheetInput}
        setSpreadsheetInput={setSpreadsheetInput}
        onConnectSpreadsheet={(id) => {
          const token = user ? (window as any)._cachedToken : null;
          getAccessToken().then(t => {
            if (t) loadSheetInfo(t, extractSpreadsheetId(id));
          });
        }}
        onSyncData={handleSyncFromSheets}
        onExportLocalToSheet={handleExportLocalToSheet}
        isSyncing={isSyncing}
        transactionsCount={transactions.length}
        onLogin={handleGoogleLogin}
        syncMessage={syncMessage}
        syncError={syncError}
      />

      {/* Mandatory Explicit Confirmation Dialog for Destructive / Mutating operations */}
      <ConfirmationModal
        isOpen={confirmModalState.isOpen}
        title={confirmModalState.title}
        message={confirmModalState.message}
        confirmLabel={confirmModalState.confirmLabel}
        cancelLabel={confirmModalState.cancelLabel}
        secondaryLabel={confirmModalState.secondaryLabel}
        isDestructive={confirmModalState.isDestructive}
        onConfirm={confirmModalState.onConfirm}
        onCancel={confirmModalState.onCancel || (() => setConfirmModalState(prev => ({ ...prev, isOpen: false })))}
        onSecondaryAction={confirmModalState.onSecondaryAction}
      />
    </div>
  );
}
