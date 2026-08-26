import React, { useState } from 'react';
import { 
  Landmark, 
  CreditCard as CardIcon, 
  Plus, 
  Wallet, 
  AlertCircle,
  CheckCircle2,
  Calendar,
  Edit2,
  Trash2,
  X,
  Check,
  Building2,
  DollarSign,
  Repeat,
  Layers,
  ChevronRight,
  ChevronLeft,
  CalendarDays,
  Clock,
  ArrowRight,
  Receipt,
  Eye,
  CreditCard,
  Sparkles,
  TrendingDown
} from 'lucide-react';
import { Account, CreditCard as CreditCardType, AccountType, Transaction } from '../types';
import { MONTH_NAMES } from '../data/initialData';

interface AccountsAndCardsViewProps {
  accounts: Account[];
  creditCards: CreditCardType[];
  transactions: Transaction[];
  selectedMonth: number;
  selectedYear: number;
  onOpenNewTransaction: () => void;
  onUpdateAccountBalance: (accountId: string, newBalance: number) => void;
  onAddAccount: (account: Omit<Account, 'id'>) => void;
  onEditAccount: (account: Account) => void;
  onDeleteAccount: (account: Account) => void;
  onAddCreditCard: (card: Omit<CreditCardType, 'id'>) => void;
  onEditCreditCard: (card: CreditCardType) => void;
  onDeleteCreditCard: (card: CreditCardType) => void;
  onPayCardInvoice?: (card: CreditCardType, sourceAccountName: string, amount: number, paymentDate: string) => void;
}

const PRESET_COLORS = [
  '#8B5CF6', // Purple / Nubank
  '#F97316', // Orange / Inter
  '#10B981', // Emerald / Green
  '#3B82F6', // Blue / Itaú / Caixa
  '#EF4444', // Red / Bradesco / Santander
  '#0EA5E9', // Sky / Rico / XP
  '#6366F1', // Indigo
  '#64748B', // Slate / Gray
  '#EAB308', // Amber / Gold
  '#EC4899', // Pink
];

export const AccountsAndCardsView: React.FC<AccountsAndCardsViewProps> = ({
  accounts,
  creditCards,
  transactions,
  selectedMonth,
  selectedYear,
  onOpenNewTransaction,
  onUpdateAccountBalance,
  onAddAccount,
  onEditAccount,
  onDeleteAccount,
  onAddCreditCard,
  onEditCreditCard,
  onDeleteCreditCard,
  onPayCardInvoice
}) => {
  // Quick inline balance editing
  const [editingAccId, setEditingAccId] = useState<string | null>(null);
  const [newBalanceInput, setNewBalanceInput] = useState<string>('');

  // Account Modal state
  const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);
  const [accountName, setAccountName] = useState('');
  const [accountInstitution, setAccountInstitution] = useState('Nubank');
  const [accountType, setAccountType] = useState<AccountType>('checking');
  const [accountBalance, setAccountBalance] = useState('');
  const [accountColor, setAccountColor] = useState('#8B5CF6');

  // Credit Card Modal state (create / edit)
  const [isCardModalOpen, setIsCardModalOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<CreditCardType | null>(null);
  const [cardName, setCardName] = useState('');
  const [cardInstitution, setCardInstitution] = useState('Nubank');
  const [cardLimit, setCardLimit] = useState('');
  const [cardInvoice, setCardInvoice] = useState('');
  const [cardClosingDay, setCardClosingDay] = useState('15');
  const [cardDueDay, setCardDueDay] = useState('22');
  const [cardColor, setCardColor] = useState('#8B5CF6');

  // Card Invoice Details Modal state
  const [selectedCardForInvoice, setSelectedCardForInvoice] = useState<CreditCardType | null>(null);
  const [invoiceViewMonth, setInvoiceViewMonth] = useState<number>(selectedMonth);
  const [invoiceViewYear, setInvoiceViewYear] = useState<number>(selectedYear);
  const [invoiceActiveTab, setInvoiceActiveTab] = useState<'items' | 'projection'>('items');

  // Pay Invoice Modal state
  const [payingCard, setPayingCard] = useState<CreditCardType | null>(null);
  const [paymentSourceAccount, setPaymentSourceAccount] = useState<string>(accounts[0]?.name || '');
  const [paymentAmount, setPaymentAmount] = useState<string>('');
  const [paymentDate, setPaymentDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [paymentSuccessMsg, setPaymentSuccessMsg] = useState<string | null>(null);

  // Month prefix string for calculations
  const currentMonthPrefix = `${selectedYear}-${String(selectedMonth + 1).padStart(2, '0')}`;

  // Helper to compute dynamic invoice for a card for any given month/year
  const getCardMonthStats = (card: CreditCardType, month: number, year: number) => {
    const prefix = `${year}-${String(month + 1).padStart(2, '0')}`;
    const cardMonthTxs = transactions.filter(
      t => t.account === card.name && t.type === 'expense' && t.date.startsWith(prefix)
    );

    const recurringTxs = cardMonthTxs.filter(t => t.isRecurring || t.recurrenceGroupId);
    const installmentTxs = cardMonthTxs.filter(t => t.isInstallment || t.installmentGroupId);
    const singleTxs = cardMonthTxs.filter(
      t => !t.isRecurring && !t.recurrenceGroupId && !t.isInstallment && !t.installmentGroupId
    );

    const recurringTotal = recurringTxs.reduce((sum, t) => sum + t.amount, 0);
    const installmentTotal = installmentTxs.reduce((sum, t) => sum + t.amount, 0);
    const singleTotal = singleTxs.reduce((sum, t) => sum + t.amount, 0);
    const calculatedTotal = recurringTotal + installmentTotal + singleTotal;

    // Use calculated total if transactions exist, else fallback to static card.currentInvoice for current month
    const isViewingCurrentMonth = month === selectedMonth && year === selectedYear;
    const finalInvoice = cardMonthTxs.length > 0 
      ? calculatedTotal 
      : (isViewingCurrentMonth ? card.currentInvoice : 0);

    return {
      allTxs: cardMonthTxs,
      recurringTxs,
      installmentTxs,
      singleTxs,
      recurringTotal,
      installmentTotal,
      singleTotal,
      totalInvoice: finalInvoice,
      txCount: cardMonthTxs.length
    };
  };

  // Top summary calculations
  const totalBankBalance = accounts.reduce((sum, a) => sum + a.balance, 0);
  const totalLimit = creditCards.reduce((sum, c) => sum + c.limit, 0);
  const totalInvoices = creditCards.reduce((sum, c) => {
    const stats = getCardMonthStats(c, selectedMonth, selectedYear);
    return sum + stats.totalInvoice;
  }, 0);
  const totalAvailableLimit = Math.max(0, totalLimit - totalInvoices);

  const handleSaveInlineBalance = (accId: string) => {
    const val = parseFloat(newBalanceInput.replace(',', '.'));
    if (!isNaN(val)) {
      onUpdateAccountBalance(accId, val);
    }
    setEditingAccId(null);
  };

  // Open Account Modal
  const openNewAccountModal = () => {
    setEditingAccount(null);
    setAccountName('');
    setAccountInstitution('Nubank');
    setAccountType('checking');
    setAccountBalance('0');
    setAccountColor('#8B5CF6');
    setIsAccountModalOpen(true);
  };

  const openEditAccountModal = (acc: Account) => {
    setEditingAccount(acc);
    setAccountName(acc.name);
    setAccountInstitution(acc.institution);
    setAccountType(acc.type);
    setAccountBalance(acc.balance.toString());
    setAccountColor(acc.color);
    setIsAccountModalOpen(true);
  };

  const handleAccountSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const balanceNum = parseFloat(accountBalance.replace(',', '.')) || 0;
    if (!accountName.trim()) return;

    if (editingAccount) {
      onEditAccount({
        ...editingAccount,
        name: accountName.trim(),
        institution: accountInstitution.trim(),
        type: accountType,
        balance: balanceNum,
        color: accountColor
      });
    } else {
      onAddAccount({
        name: accountName.trim(),
        institution: accountInstitution.trim(),
        type: accountType,
        balance: balanceNum,
        color: accountColor
      });
    }
    setIsAccountModalOpen(false);
  };

  // Open Credit Card Modal
  const openNewCardModal = () => {
    setEditingCard(null);
    setCardName('');
    setCardInstitution('Nubank');
    setCardLimit('5000');
    setCardInvoice('0');
    setCardClosingDay('15');
    setCardDueDay('22');
    setCardColor('#8B5CF6');
    setIsCardModalOpen(true);
  };

  const openEditCardModal = (card: CreditCardType) => {
    setEditingCard(card);
    setCardName(card.name);
    setCardInstitution(card.institution);
    setCardLimit(card.limit.toString());
    setCardInvoice(card.currentInvoice.toString());
    setCardClosingDay(card.closingDay.toString());
    setCardDueDay(card.dueDay.toString());
    setCardColor(card.color);
    setIsCardModalOpen(true);
  };

  const handleCardSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const limitNum = parseFloat(cardLimit.replace(',', '.')) || 0;
    const invoiceNum = parseFloat(cardInvoice.replace(',', '.')) || 0;
    const closing = parseInt(cardClosingDay, 10) || 15;
    const due = parseInt(cardDueDay, 10) || 22;

    if (!cardName.trim()) return;

    if (editingCard) {
      onEditCreditCard({
        ...editingCard,
        name: cardName.trim(),
        institution: cardInstitution.trim(),
        limit: limitNum,
        currentInvoice: invoiceNum,
        closingDay: Math.min(31, Math.max(1, closing)),
        dueDay: Math.min(31, Math.max(1, due)),
        color: cardColor
      });
    } else {
      onAddCreditCard({
        name: cardName.trim(),
        institution: cardInstitution.trim(),
        limit: limitNum,
        currentInvoice: invoiceNum,
        closingDay: Math.min(31, Math.max(1, closing)),
        dueDay: Math.min(31, Math.max(1, due)),
        color: cardColor
      });
    }
    setIsCardModalOpen(false);
  };

  // Open Invoice View Modal
  const openInvoiceModal = (card: CreditCardType) => {
    setSelectedCardForInvoice(card);
    setInvoiceViewMonth(selectedMonth);
    setInvoiceViewYear(selectedYear);
    setInvoiceActiveTab('items');
  };

  // Open Pay Invoice Modal
  const openPayInvoiceModal = (card: CreditCardType, currentAmount: number) => {
    setPayingCard(card);
    setPaymentSourceAccount(accounts[0]?.name || '');
    setPaymentAmount(currentAmount > 0 ? currentAmount.toFixed(2) : '0.00');
    setPaymentDate(new Date().toISOString().split('T')[0]);
    setPaymentSuccessMsg(null);
  };

  const handleConfirmPayInvoice = (e: React.FormEvent) => {
    e.preventDefault();
    if (!payingCard) return;
    const numAmount = parseFloat(paymentAmount.replace(',', '.')) || 0;
    if (numAmount <= 0) return;

    if (onPayCardInvoice) {
      onPayCardInvoice(payingCard, paymentSourceAccount, numAmount, paymentDate);
    } else {
      // Fallback deduction
      const srcAcc = accounts.find(a => a.name === paymentSourceAccount);
      if (srcAcc) {
        onUpdateAccountBalance(srcAcc.id, srcAcc.balance - numAmount);
      }
    }

    setPaymentSuccessMsg(`Fatura do cartão "${payingCard.name}" liquidada com sucesso!`);
    setTimeout(() => {
      setPayingCard(null);
      setPaymentSuccessMsg(null);
    }, 1500);
  };

  const getAccountTypeLabel = (type: AccountType) => {
    switch (type) {
      case 'checking': return 'Conta Corrente';
      case 'savings': return 'Poupança';
      case 'investment': return 'Investimentos';
      case 'cash': return 'Dinheiro em Espécie';
      case 'credit': return 'Crédito';
      default: return 'Conta';
    }
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Top summary cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Total in Accounts */}
        <div className="p-6 rounded-2xl bg-gradient-to-br from-slate-900 to-slate-800 text-white shadow-lg space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2.5">
              <div className="p-2.5 rounded-xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                <Landmark className="w-5 h-5" />
              </div>
              <div>
                <span className="text-xs uppercase font-semibold text-slate-300">Saldo Total em Contas</span>
                <h3 className="text-xl font-bold">R$ {totalBankBalance.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</h3>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <button
                id="add-account-top-btn"
                onClick={openNewAccountModal}
                className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold shadow-sm transition flex items-center space-x-1"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Nova Conta</span>
              </button>
            </div>
          </div>

          <div className="flex items-center justify-between text-xs text-slate-300 pt-2 border-t border-white/10">
            <span>{accounts.length} contas bancárias ativas</span>
            <span className="text-emerald-400 font-medium">Liquidez Imediata</span>
          </div>
        </div>

        {/* Total Credit Cards */}
        <div className="p-6 rounded-2xl bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 text-white shadow-lg space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2.5">
              <div className="p-2.5 rounded-xl bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
                <CardIcon className="w-5 h-5" />
              </div>
              <div>
                <span className="text-xs uppercase font-semibold text-slate-300">
                  Faturas em {MONTH_NAMES[selectedMonth]}
                </span>
                <h3 className="text-xl font-bold">R$ {totalInvoices.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</h3>
              </div>
            </div>
            <button
              id="add-card-top-btn"
              onClick={openNewCardModal}
              className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold shadow-sm transition flex items-center space-x-1"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Novo Cartão</span>
            </button>
          </div>

          <div className="flex items-center justify-between text-xs text-slate-300 pt-2 border-t border-white/10">
            <span>{creditCards.length} cartões gerenciados</span>
            <span className="text-indigo-300 font-medium">Limite Total: R$ {totalLimit.toLocaleString('pt-BR')}</span>
          </div>
        </div>
      </div>

      {/* Credit Cards Section with Real-Time Invoice Sync */}
      <div className="space-y-4">
        <div className="flex items-center justify-between flex-wrap gap-2">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-white flex items-center space-x-2">
              <CardIcon className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
              <span>Cartões de Crédito & Faturas Integradas</span>
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Lançamentos fixos e parcelados são integrados automaticamente na fatura de cada mês
            </p>
          </div>

          <div className="flex items-center space-x-2">
            <button
              id="open-new-card-btn"
              onClick={openNewCardModal}
              className="px-3 py-1.5 rounded-xl bg-indigo-50 dark:bg-indigo-950/40 text-indigo-700 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800 hover:bg-indigo-100 dark:hover:bg-indigo-900/50 text-xs font-semibold transition flex items-center space-x-1.5"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Adicionar Cartão</span>
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {creditCards.map((card) => {
            const stats = getCardMonthStats(card, selectedMonth, selectedYear);
            const usagePercent = card.limit > 0 ? Math.min(100, Math.round((stats.totalInvoice / card.limit) * 100)) : 0;
            const available = Math.max(0, card.limit - stats.totalInvoice);

            return (
              <div
                key={card.id}
                className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4 hover:border-slate-300 dark:hover:border-slate-700 transition flex flex-col justify-between"
              >
                {/* Header of the Card */}
                <div className="space-y-3">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center space-x-3">
                      <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-sm flex-shrink-0"
                        style={{ backgroundColor: card.color || '#4F46E5' }}
                      >
                        <CardIcon className="w-5 h-5" />
                      </div>
                      <div>
                        <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                          {card.name}
                        </h3>
                        <span className="text-[11px] text-slate-500 font-semibold uppercase tracking-wider">
                          {card.institution}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center space-x-1">
                      <button
                        onClick={() => openEditCardModal(card)}
                        className="p-1.5 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                        title="Editar Cartão"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => onDeleteCreditCard(card)}
                        className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-rose-50 dark:hover:bg-rose-950/40 transition"
                        title="Excluir Cartão"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  {/* Invoice & Limit Values */}
                  <div className="grid grid-cols-2 gap-2 pt-1 text-xs">
                    <div>
                      <span className="text-[11px] text-slate-500 block">
                        Fatura de {MONTH_NAMES[selectedMonth]}
                      </span>
                      <span className="text-lg font-black text-rose-600 dark:text-rose-400">
                        R$ {stats.totalInvoice.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                    <div className="text-right">
                      <span className="text-[11px] text-slate-500 block">Limite Total</span>
                      <span className="text-lg font-black text-slate-900 dark:text-white">
                        R$ {card.limit.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  </div>

                  {/* Limit Usage Progress Bar */}
                  <div className="space-y-1.5">
                    <div className="w-full h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-300 ${
                          usagePercent > 85 ? 'bg-rose-500' : usagePercent > 50 ? 'bg-amber-500' : 'bg-indigo-500'
                        }`}
                        style={{ width: `${usagePercent}%` }}
                      />
                    </div>
                    <div className="flex items-center justify-between text-[11px] text-slate-500">
                      <span>{usagePercent}% do limite</span>
                      <span className="font-semibold text-slate-700 dark:text-slate-300">
                        Disponível: R$ {available.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  </div>

                  {/* Automatic Invoice Composition Chips */}
                  <div className="pt-2 border-t border-slate-100 dark:border-slate-800 grid grid-cols-3 gap-1.5 text-center">
                    <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 text-[11px]">
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">Fixos/Assin.</span>
                      <span className="font-bold text-emerald-600 dark:text-emerald-400">
                        R$ {stats.recurringTotal.toFixed(2)}
                      </span>
                    </div>
                    <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 text-[11px]">
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">Parcelas</span>
                      <span className="font-bold text-indigo-600 dark:text-indigo-400">
                        R$ {stats.installmentTotal.toFixed(2)}
                      </span>
                    </div>
                    <div className="p-2 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 text-[11px]">
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">À Vista</span>
                      <span className="font-bold text-slate-700 dark:text-slate-300">
                        R$ {stats.singleTotal.toFixed(2)}
                      </span>
                    </div>
                  </div>

                  {/* Card Dates */}
                  <div className="grid grid-cols-2 gap-2 text-xs text-slate-500 dark:text-slate-400 pt-1">
                    <div className="flex items-center space-x-1.5">
                      <Calendar className="w-3.5 h-3.5 text-slate-400" />
                      <span>Fecha dia: <strong className="text-slate-900 dark:text-white">{card.closingDay}</strong></span>
                    </div>
                    <div className="flex items-center space-x-1.5">
                      <Calendar className="w-3.5 h-3.5 text-slate-400" />
                      <span>Vence dia: <strong className="text-slate-900 dark:text-white">{card.dueDay}</strong></span>
                    </div>
                  </div>
                </div>

                {/* Card Action Buttons */}
                <div className="pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between gap-2">
                  <button
                    id={`view-invoice-${card.id}-btn`}
                    onClick={() => openInvoiceModal(card)}
                    className="flex-1 px-3 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs font-semibold transition flex items-center justify-center space-x-1.5"
                  >
                    <Receipt className="w-3.5 h-3.5 text-indigo-500" />
                    <span>Ver Fatura ({stats.txCount})</span>
                  </button>

                  <button
                    id={`pay-invoice-${card.id}-btn`}
                    onClick={() => openPayInvoiceModal(card, stats.totalInvoice)}
                    className="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold shadow-sm transition flex items-center space-x-1"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Pagar Fatura</span>
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Accounts Section */}
      <div className="space-y-4 pt-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-white flex items-center space-x-2">
              <Landmark className="w-4 h-4 text-emerald-600" />
              <span>Contas Bancárias & Carteiras</span>
            </h2>
            <p className="text-xs text-slate-500">
              Gerencie seus saldos bancários, contas correntes e dinheiro em espécie
            </p>
          </div>

          <button
            id="open-new-account-btn"
            onClick={openNewAccountModal}
            className="px-3 py-1.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800 hover:bg-emerald-100 dark:hover:bg-emerald-900/50 text-xs font-semibold transition flex items-center space-x-1.5"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Adicionar Conta</span>
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {accounts.map((acc) => {
            const isEditing = editingAccId === acc.id;
            return (
              <div
                key={acc.id}
                className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4 flex flex-col justify-between hover:border-slate-300 dark:hover:border-slate-700 transition"
              >
                <div className="space-y-3">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center space-x-2.5">
                      <div
                        className="w-3.5 h-3.5 rounded-full flex-shrink-0"
                        style={{ backgroundColor: acc.color }}
                      />
                      <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                        {acc.institution}
                      </span>
                    </div>

                    <div className="flex items-center space-x-1">
                      <button
                        onClick={() => openEditAccountModal(acc)}
                        className="p-1 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 rounded transition"
                        title="Editar Conta"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => onDeleteAccount(acc)}
                        className="p-1 text-slate-400 hover:text-rose-600 rounded transition"
                        title="Excluir Conta"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                      {acc.name}
                    </h3>
                    <span className="text-[11px] text-slate-500">
                      {getAccountTypeLabel(acc.type)}
                    </span>
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-100 dark:border-slate-800">
                  <span className="text-[11px] text-slate-500 block mb-1">Saldo Atual</span>
                  {isEditing ? (
                    <div className="flex items-center space-x-1">
                      <input
                        type="number"
                        step="0.01"
                        value={newBalanceInput}
                        onChange={(e) => setNewBalanceInput(e.target.value)}
                        className="w-full px-2 py-1 text-xs border rounded-lg border-emerald-500 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                      />
                      <button
                        onClick={() => handleSaveInlineBalance(acc.id)}
                        className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold"
                      >
                        Salvar
                      </button>
                      <button
                        onClick={() => setEditingAccId(null)}
                        className="p-1 text-slate-400 hover:text-slate-600"
                      >
                        <X className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ) : (
                    <div className="flex items-center justify-between">
                      <span className={`text-base font-black ${acc.balance >= 0 ? 'text-slate-900 dark:text-white' : 'text-rose-600'}`}>
                        R$ {acc.balance.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                      </span>
                      <button
                        onClick={() => {
                          setEditingAccId(acc.id);
                          setNewBalanceInput(acc.balance.toString());
                        }}
                        className="text-[11px] text-emerald-600 dark:text-emerald-400 hover:underline font-semibold"
                      >
                        Ajustar
                      </button>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* DETAILED CARD INVOICE MODAL */}
      {selectedCardForInvoice && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/70 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]">
            {/* Modal Header */}
            <div className="p-5 border-b border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/60 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <div 
                  className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-sm"
                  style={{ backgroundColor: selectedCardForInvoice.color || '#4F46E5' }}
                >
                  <CardIcon className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-900 dark:text-white flex items-center space-x-2">
                    <span>Fatura: {selectedCardForInvoice.name}</span>
                    <span className="text-xs font-semibold px-2 py-0.5 rounded-md bg-indigo-100 dark:bg-indigo-900/60 text-indigo-700 dark:text-indigo-300">
                      {selectedCardForInvoice.institution}
                    </span>
                  </h3>
                  <p className="text-xs text-slate-500">
                    Fechamento dia {selectedCardForInvoice.closingDay} • Vencimento dia {selectedCardForInvoice.dueDay}
                  </p>
                </div>
              </div>

              <button
                onClick={() => setSelectedCardForInvoice(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Month Switcher & Tabs */}
            <div className="px-5 py-3 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between flex-wrap gap-2 bg-white dark:bg-slate-900">
              {/* Month Navigation */}
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => {
                    if (invoiceViewMonth === 0) {
                      setInvoiceViewMonth(11);
                      setInvoiceViewYear(prev => prev - 1);
                    } else {
                      setInvoiceViewMonth(prev => prev - 1);
                    }
                  }}
                  className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>

                <span className="text-xs font-bold text-slate-800 dark:text-slate-200 min-w-[140px] text-center">
                  {MONTH_NAMES[invoiceViewMonth]} {invoiceViewYear}
                </span>

                <button
                  onClick={() => {
                    if (invoiceViewMonth === 11) {
                      setInvoiceViewMonth(0);
                      setInvoiceViewYear(prev => prev + 1);
                    } else {
                      setInvoiceViewMonth(prev => prev + 1);
                    }
                  }}
                  className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>

              {/* View Tabs */}
              <div className="flex items-center p-1 bg-slate-100 dark:bg-slate-800 rounded-xl">
                <button
                  onClick={() => setInvoiceActiveTab('items')}
                  className={`px-3 py-1 text-xs font-semibold rounded-lg transition ${
                    invoiceActiveTab === 'items'
                      ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-white shadow-sm'
                      : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
                  }`}
                >
                  Lançamentos do Mês
                </button>
                <button
                  onClick={() => setInvoiceActiveTab('projection')}
                  className={`px-3 py-1 text-xs font-semibold rounded-lg transition ${
                    invoiceActiveTab === 'projection'
                      ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-white shadow-sm'
                      : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
                  }`}
                >
                  Projeção Futura
                </button>
              </div>
            </div>

            {/* Modal Body */}
            <div className="p-5 overflow-y-auto space-y-4 flex-1">
              {(() => {
                const currentStats = getCardMonthStats(selectedCardForInvoice, invoiceViewMonth, invoiceViewYear);

                if (invoiceActiveTab === 'projection') {
                  // Next 6 months projection
                  const projectionMonths = Array.from({ length: 6 }, (_, idx) => {
                    const m = (selectedMonth + idx) % 12;
                    const y = selectedYear + Math.floor((selectedMonth + idx) / 12);
                    const st = getCardMonthStats(selectedCardForInvoice, m, y);
                    return { month: m, year: y, ...st };
                  });

                  return (
                    <div className="space-y-4">
                      <div className="p-4 rounded-xl bg-indigo-50/70 dark:bg-indigo-950/30 border border-indigo-200 dark:border-indigo-800 text-xs">
                        <div className="flex items-center space-x-2 text-indigo-900 dark:text-indigo-200 font-bold mb-1">
                          <Sparkles className="w-4 h-4 text-indigo-600" />
                          <span>Previsão de Faturas Futuras (Próximos 6 Meses)</span>
                        </div>
                        <p className="text-slate-600 dark:text-slate-400 text-[11px]">
                          Esta projeção calcula automaticamente todos os seus <strong>lançamentos fixos mensais</strong> (assinaturas, contas) e as <strong>parcelas futuras</strong> já agendadas para este cartão.
                        </p>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        {projectionMonths.map((proj, pIdx) => (
                          <div
                            key={pIdx}
                            className={`p-4 rounded-xl border transition ${
                              proj.month === invoiceViewMonth && proj.year === invoiceViewYear
                                ? 'border-indigo-500 bg-indigo-50/40 dark:bg-indigo-950/40 ring-2 ring-indigo-500/20'
                                : 'border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900'
                            }`}
                          >
                            <div className="flex items-center justify-between mb-2">
                              <span className="text-xs font-bold text-slate-800 dark:text-slate-200">
                                {MONTH_NAMES[proj.month]} {proj.year}
                              </span>
                              <span className="text-xs font-black text-rose-600 dark:text-rose-400">
                                R$ {proj.totalInvoice.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                              </span>
                            </div>

                            <div className="space-y-1 text-[11px] text-slate-500 pt-2 border-t border-slate-100 dark:border-slate-800">
                              <div className="flex justify-between">
                                <span>🔄 Lançamentos Fixos:</span>
                                <span className="font-semibold text-emerald-600">R$ {proj.recurringTotal.toFixed(2)}</span>
                              </div>
                              <div className="flex justify-between">
                                <span>💳 Parcelas no Mês:</span>
                                <span className="font-semibold text-indigo-600">R$ {proj.installmentTotal.toFixed(2)}</span>
                              </div>
                              <div className="flex justify-between">
                                <span>⚡ Compras Avulsas:</span>
                                <span className="font-semibold text-slate-600 dark:text-slate-400">R$ {proj.singleTotal.toFixed(2)}</span>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  );
                }

                // Default Tab: List of items in this month's invoice
                return (
                  <div className="space-y-4">
                    {/* Invoice KPI Header */}
                    <div className="p-4 rounded-2xl bg-gradient-to-br from-slate-900 to-slate-800 text-white flex items-center justify-between">
                      <div>
                        <span className="text-[11px] uppercase font-semibold text-slate-400">
                          Total da Fatura ({MONTH_NAMES[invoiceViewMonth]} {invoiceViewYear})
                        </span>
                        <div className="text-2xl font-black text-rose-400">
                          R$ {currentStats.totalInvoice.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                        </div>
                      </div>
                      <div className="text-right text-xs text-slate-300 space-y-0.5">
                        <div>Vencimento: <strong>{selectedCardForInvoice.dueDay}/{String(invoiceViewMonth + 1).padStart(2, '0')}</strong></div>
                        <div>Fechamento: <strong>{selectedCardForInvoice.closingDay}/{String(invoiceViewMonth + 1).padStart(2, '0')}</strong></div>
                      </div>
                    </div>

                    {/* Breakdown section */}
                    {currentStats.allTxs.length === 0 ? (
                      <div className="text-center py-10 text-slate-400 text-xs space-y-3">
                        <Receipt className="w-8 h-8 mx-auto text-slate-300 dark:text-slate-600" />
                        <p>Nenhum lançamento lançado nesta fatura de {MONTH_NAMES[invoiceViewMonth]}.</p>
                        <button
                          onClick={() => {
                            setSelectedCardForInvoice(null);
                            onOpenNewTransaction();
                          }}
                          className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold inline-flex items-center space-x-1"
                        >
                          <Plus className="w-3.5 h-3.5" />
                          <span>Lançar Compra / Fixo no Cartão</span>
                        </button>
                      </div>
                    ) : (
                      <div className="space-y-3">
                        {/* 1. Recurring / Fixed Charges */}
                        {currentStats.recurringTxs.length > 0 && (
                          <div className="space-y-2">
                            <div className="flex items-center space-x-2 text-xs font-bold text-emerald-700 dark:text-emerald-400">
                              <Repeat className="w-3.5 h-3.5" />
                              <span>Lançamentos Fixos Recorrentes ({currentStats.recurringTxs.length})</span>
                            </div>
                            <div className="space-y-1.5">
                              {currentStats.recurringTxs.map(t => (
                                <div
                                  key={t.id}
                                  className="p-3 rounded-xl border border-emerald-100 dark:border-emerald-900/40 bg-emerald-50/40 dark:bg-emerald-950/20 flex items-center justify-between"
                                >
                                  <div className="flex items-center space-x-2.5 min-w-0">
                                    <div className="p-1.5 rounded-lg bg-emerald-600 text-white text-xs">
                                      <Repeat className="w-3.5 h-3.5" />
                                    </div>
                                    <div className="truncate">
                                      <span className="text-xs font-bold text-slate-900 dark:text-white truncate block">
                                        {t.description}
                                      </span>
                                      <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-semibold">
                                        Fixo Todo Mês • {t.category} • Dia {t.date.split('-')[2]}
                                      </span>
                                    </div>
                                  </div>
                                  <span className="text-xs font-black text-slate-900 dark:text-white">
                                    R$ {t.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                                  </span>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* 2. Installments */}
                        {currentStats.installmentTxs.length > 0 && (
                          <div className="space-y-2 pt-2">
                            <div className="flex items-center space-x-2 text-xs font-bold text-indigo-700 dark:text-indigo-400">
                              <Layers className="w-3.5 h-3.5" />
                              <span>Compras Parceladas no Cartão ({currentStats.installmentTxs.length})</span>
                            </div>
                            <div className="space-y-1.5">
                              {currentStats.installmentTxs.map(t => (
                                <div
                                  key={t.id}
                                  className="p-3 rounded-xl border border-indigo-100 dark:border-indigo-900/40 bg-indigo-50/40 dark:bg-indigo-950/20 flex items-center justify-between"
                                >
                                  <div className="flex items-center space-x-2.5 min-w-0">
                                    <div className="p-1.5 rounded-lg bg-indigo-600 text-white text-xs">
                                      <Layers className="w-3.5 h-3.5" />
                                    </div>
                                    <div className="truncate">
                                      <span className="text-xs font-bold text-slate-900 dark:text-white truncate block">
                                        {t.description}
                                      </span>
                                      <span className="text-[10px] text-indigo-600 dark:text-indigo-400 font-semibold">
                                        Parcela {t.installmentCurrent || '1'}/{t.installmentTotal || '10'} • {t.category} • Dia {t.date.split('-')[2]}
                                      </span>
                                    </div>
                                  </div>
                                  <span className="text-xs font-black text-slate-900 dark:text-white">
                                    R$ {t.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                                  </span>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* 3. Single / À Vista Charges */}
                        {currentStats.singleTxs.length > 0 && (
                          <div className="space-y-2 pt-2">
                            <div className="flex items-center space-x-2 text-xs font-bold text-slate-700 dark:text-slate-300">
                              <CardIcon className="w-3.5 h-3.5" />
                              <span>Compras À Vista / Avulsas ({currentStats.singleTxs.length})</span>
                            </div>
                            <div className="space-y-1.5">
                              {currentStats.singleTxs.map(t => (
                                <div
                                  key={t.id}
                                  className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-800/40 flex items-center justify-between"
                                >
                                  <div className="flex items-center space-x-2.5 min-w-0">
                                    <div className="p-1.5 rounded-lg bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-200 text-xs">
                                      <CardIcon className="w-3.5 h-3.5" />
                                    </div>
                                    <div className="truncate">
                                      <span className="text-xs font-bold text-slate-900 dark:text-white truncate block">
                                        {t.description}
                                      </span>
                                      <span className="text-[10px] text-slate-500">
                                        {t.category} • Dia {t.date.split('-')[2]}
                                      </span>
                                    </div>
                                  </div>
                                  <span className="text-xs font-black text-slate-900 dark:text-white">
                                    R$ {t.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                                  </span>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })()}
            </div>

            {/* Modal Footer */}
            <div className="p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/60 flex items-center justify-between">
              <button
                onClick={() => setSelectedCardForInvoice(null)}
                className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                Fechar
              </button>

              <button
                onClick={() => {
                  const cardStats = getCardMonthStats(selectedCardForInvoice, invoiceViewMonth, invoiceViewYear);
                  setSelectedCardForInvoice(null);
                  openPayInvoiceModal(selectedCardForInvoice, cardStats.totalInvoice);
                }}
                className="px-4 py-2 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm flex items-center space-x-1.5"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Pagar Esta Fatura</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* PAY INVOICE MODAL */}
      {payingCard && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/70 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="p-2 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600">
                  <CheckCircle2 className="w-5 h-5" />
                </div>
                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  Pagar Fatura do Cartão
                </h3>
              </div>
              <button
                onClick={() => setPayingCard(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {paymentSuccessMsg ? (
              <div className="p-4 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200 rounded-xl text-center text-xs font-semibold space-y-1">
                <CheckCircle2 className="w-6 h-6 mx-auto text-emerald-600" />
                <p>{paymentSuccessMsg}</p>
              </div>
            ) : (
              <form onSubmit={handleConfirmPayInvoice} className="space-y-4">
                <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl border border-slate-100 dark:border-slate-800 text-xs">
                  <span className="text-slate-500 block text-[11px]">Cartão de Crédito</span>
                  <span className="font-bold text-slate-900 dark:text-white text-sm">{payingCard.name} ({payingCard.institution})</span>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Conta de Origem para Pagamento *
                  </label>
                  <select
                    value={paymentSourceAccount}
                    onChange={(e) => setPaymentSourceAccount(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500"
                  >
                    {accounts.map(acc => (
                      <option key={acc.id} value={acc.name}>
                        🏛️ {acc.name} (Saldo: R$ {acc.balance.toLocaleString('pt-BR', { minimumFractionDigits: 2 })})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Valor a Pagar (R$) *
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-bold text-slate-400">R$</span>
                    <input
                      type="number"
                      step="0.01"
                      value={paymentAmount}
                      onChange={(e) => setPaymentAmount(e.target.value)}
                      required
                      className="w-full pl-9 pr-3 py-2 text-xs font-bold rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Data do Pagamento *
                  </label>
                  <input
                    type="date"
                    value={paymentDate}
                    onChange={(e) => setPaymentDate(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                <div className="flex items-center justify-end space-x-2 pt-2 border-t border-slate-100 dark:border-slate-800">
                  <button
                    type="button"
                    onClick={() => setPayingCard(null)}
                    className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    className="px-5 py-2 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm"
                  >
                    Confirmar Pagamento
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Account Modal (Add / Edit) */}
      {isAccountModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="p-2 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600">
                  <Landmark className="w-5 h-5" />
                </div>
                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  {editingAccount ? 'Editar Conta Bancária' : 'Nova Conta Bancária / Carteira'}
                </h3>
              </div>
              <button
                onClick={() => setIsAccountModalOpen(false)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleAccountSubmit} className="space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Nome Identificador da Conta *
                </label>
                <input
                  type="text"
                  placeholder="Ex: Nubank Principal, Itaú Corrente, Carteira..."
                  value={accountName}
                  onChange={(e) => setAccountName(e.target.value)}
                  required
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Instituição / Banco *
                  </label>
                  <input
                    type="text"
                    placeholder="Ex: Nubank, Inter, Itaú..."
                    value={accountInstitution}
                    onChange={(e) => setAccountInstitution(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Tipo de Conta *
                  </label>
                  <select
                    value={accountType}
                    onChange={(e) => setAccountType(e.target.value as AccountType)}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                    <option value="checking">Conta Corrente</option>
                    <option value="savings">Poupança</option>
                    <option value="investment">Investimentos</option>
                    <option value="cash">Dinheiro em Espécie</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Saldo Inicial / Atual (R$) *
                </label>
                <div className="relative">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-bold text-slate-400">R$</span>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    value={accountBalance}
                    onChange={(e) => setAccountBalance(e.target.value)}
                    required
                    className="w-full pl-9 pr-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Cor da Conta
                </label>
                <div className="flex items-center space-x-2">
                  {PRESET_COLORS.map((c) => (
                    <button
                      key={c}
                      type="button"
                      onClick={() => setAccountColor(c)}
                      className={`w-6 h-6 rounded-full transition-transform ${
                        accountColor === c ? 'scale-125 ring-2 ring-emerald-500 ring-offset-2 dark:ring-offset-slate-900' : 'hover:scale-110'
                      }`}
                      style={{ backgroundColor: c }}
                    />
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-end space-x-2 pt-3 border-t border-slate-100 dark:border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsAccountModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm"
                >
                  {editingAccount ? 'Salvar Alterações' : 'Criar Conta'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Credit Card Modal (Add / Edit) */}
      {isCardModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 p-6 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className="p-2 rounded-xl bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600">
                  <CardIcon className="w-5 h-5" />
                </div>
                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  {editingCard ? 'Editar Cartão de Crédito' : 'Novo Cartão de Crédito'}
                </h3>
              </div>
              <button
                onClick={() => setIsCardModalOpen(false)}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCardSubmit} className="space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Nome do Cartão *
                </label>
                <input
                  type="text"
                  placeholder="Ex: Nubank Ultravioleta, Inter Black, XP..."
                  value={cardName}
                  onChange={(e) => setCardName(e.target.value)}
                  required
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Emissor / Instituição *
                </label>
                <input
                  type="text"
                  placeholder="Ex: Nubank, Inter, XP, Itaú..."
                  value={cardInstitution}
                  onChange={(e) => setCardInstitution(e.target.value)}
                  required
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Limite Total (R$) *
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-bold text-slate-400">R$</span>
                    <input
                      type="number"
                      step="0.01"
                      placeholder="5000.00"
                      value={cardLimit}
                      onChange={(e) => setCardLimit(e.target.value)}
                      required
                      className="w-full pl-9 pr-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Fatura Inicial (R$)
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-bold text-slate-400">R$</span>
                    <input
                      type="number"
                      step="0.01"
                      placeholder="0.00"
                      value={cardInvoice}
                      onChange={(e) => setCardInvoice(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Dia do Fechamento (1-31) *
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    value={cardClosingDay}
                    onChange={(e) => setCardClosingDay(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Dia do Vencimento (1-31) *
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="31"
                    value={cardDueDay}
                    onChange={(e) => setCardDueDay(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Cor do Cartão
                </label>
                <div className="flex items-center space-x-2">
                  {PRESET_COLORS.map((c) => (
                    <button
                      key={c}
                      type="button"
                      onClick={() => setCardColor(c)}
                      className={`w-6 h-6 rounded-full transition-transform ${
                        cardColor === c ? 'scale-125 ring-2 ring-indigo-500 ring-offset-2 dark:ring-offset-slate-900' : 'hover:scale-110'
                      }`}
                      style={{ backgroundColor: c }}
                    />
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-end space-x-2 pt-3 border-t border-slate-100 dark:border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsCardModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-semibold rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white shadow-sm"
                >
                  {editingCard ? 'Salvar Alterações' : 'Criar Cartão'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
