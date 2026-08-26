import React, { useState, useEffect } from 'react';
import { 
  X, 
  ArrowUpCircle, 
  ArrowDownCircle, 
  ArrowLeftRight, 
  Calendar, 
  FileSpreadsheet, 
  Tag, 
  CreditCard as CardIcon, 
  Check,
  Layers,
  Repeat,
  Info,
  CalendarRange,
  DollarSign,
  Calculator
} from 'lucide-react';
import { Account, Category, CreditCard, Transaction, TransactionType } from '../types';

interface TransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (transactions: (Omit<Transaction, 'id'> & { id?: string })[], saveToSheet: boolean) => void;
  initialTransaction?: Transaction | null;
  categories: Category[];
  accounts: Account[];
  creditCards: CreditCard[];
  isGoogleConnected: boolean;
  onOpenCreateCategory?: (defaultType?: 'expense' | 'income') => void;
}

// Utility to increment months accurately
function addMonthsToDate(dateStr: string, monthsToAdd: number): string {
  const [yearStr, monthStr, dayStr] = dateStr.split('-');
  const baseYear = parseInt(yearStr, 10);
  const baseMonth = parseInt(monthStr, 10) - 1; // 0-11
  const baseDay = parseInt(dayStr, 10);

  const target = new Date(baseYear, baseMonth + monthsToAdd, 1);
  const targetYear = target.getFullYear();
  const targetMonth = target.getMonth();

  const maxDay = new Date(targetYear, targetMonth + 1, 0).getDate();
  const actualDay = Math.min(baseDay, maxDay);

  const y = targetYear;
  const m = String(targetMonth + 1).padStart(2, '0');
  const d = String(actualDay).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export const TransactionModal: React.FC<TransactionModalProps> = ({
  isOpen,
  onClose,
  onSave,
  initialTransaction,
  categories,
  accounts,
  creditCards,
  isGoogleConnected,
  onOpenCreateCategory
}) => {
  const [type, setType] = useState<TransactionType>('expense');
  const [description, setDescription] = useState('');
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [category, setCategory] = useState('');
  const [account, setAccount] = useState('');
  const [targetAccount, setTargetAccount] = useState('');
  const [status, setStatus] = useState<'completed' | 'pending'>('completed');
  const [notes, setNotes] = useState('');
  const [saveToSheet, setSaveToSheet] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Special Launch Modes: 'single' | 'installment' | 'recurring'
  const [launchMode, setLaunchMode] = useState<'single' | 'installment' | 'recurring'>('single');

  // Installment configuration state
  const [installmentValueMode, setInstallmentValueMode] = useState<'per_installment' | 'total'>('per_installment');
  const [totalInstallments, setTotalInstallments] = useState<number>(10);
  const [currentInstallment, setCurrentInstallment] = useState<number>(1); // e.g., if 10 total and starts at 1, or if 4 remaining out of 10 -> current is 7

  // Recurring configuration state
  const [recurrenceScope, setRecurrenceScope] = useState<'end_of_year' | 'full_year' | 'custom'>('end_of_year');
  const [recurrenceCustomMonths, setRecurrenceCustomMonths] = useState<number>(12);

  useEffect(() => {
    if (initialTransaction) {
      setType(initialTransaction.type);
      setDescription(initialTransaction.description);
      setAmount(initialTransaction.amount.toString());
      setDate(initialTransaction.date);
      setCategory(initialTransaction.category);
      setAccount(initialTransaction.account);
      setTargetAccount(initialTransaction.targetAccount || '');
      setStatus(initialTransaction.status);
      setNotes(initialTransaction.notes || '');

      if (initialTransaction.isInstallment) {
        setLaunchMode('installment');
        setTotalInstallments(initialTransaction.installmentTotal || 10);
        setCurrentInstallment(initialTransaction.installmentCurrent || 1);
        setInstallmentValueMode('per_installment');
      } else if (initialTransaction.isRecurring) {
        setLaunchMode('recurring');
        setRecurrenceScope('end_of_year');
      } else {
        setLaunchMode('single');
      }
    } else {
      // Default reset
      setType('expense');
      setDescription('');
      setAmount('');
      setDate(new Date().toISOString().split('T')[0]);
      const defaultExpenseCat = categories.find(c => c.type === 'expense')?.name || 'Moradia & Aluguel';
      setCategory(defaultExpenseCat);
      setAccount(creditCards[0]?.name || accounts[0]?.name || 'Nubank Conta');
      setTargetAccount(accounts[1]?.name || '');
      setStatus('completed');
      setNotes('');
      setLaunchMode('single');
      setInstallmentValueMode('per_installment');
      setTotalInstallments(10);
      setCurrentInstallment(1);
      setRecurrenceScope('end_of_year');
      setRecurrenceCustomMonths(12);
      setError(null);
    }
  }, [initialTransaction, isOpen, categories, accounts, creditCards]);

  if (!isOpen) return null;

  const filteredCategories = categories.filter(c => c.type === (type === 'income' ? 'income' : 'expense'));
  const selectedCard = creditCards.find(c => c.name === account);
  const selectedAccount = accounts.find(a => a.name === account);

  // Quick installment math helpers
  const parsedAmountInput = parseFloat(amount.replace(',', '.')) || 0;
  
  // Installment calculations
  const remainingInstallmentsCount = Math.max(1, totalInstallments - currentInstallment + 1);
  const calculatedPerInstallment = installmentValueMode === 'per_installment'
    ? parsedAmountInput
    : (totalInstallments > 0 ? parsedAmountInput / totalInstallments : 0);
  const calculatedTotalAmount = installmentValueMode === 'per_installment'
    ? parsedAmountInput * totalInstallments
    : parsedAmountInput;

  // Recurring calculations
  let recurringMonthsCount = 1;
  const currentMonthIdx = parseInt(date.split('-')[1], 10) - 1; // 0-11
  if (recurrenceScope === 'end_of_year') {
    recurringMonthsCount = Math.max(1, 12 - currentMonthIdx);
  } else if (recurrenceScope === 'full_year') {
    recurringMonthsCount = 12;
  } else {
    recurringMonthsCount = Math.max(1, recurrenceCustomMonths);
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!description.trim()) {
      setError('Por favor, informe a descrição do lançamento.');
      return;
    }
    if (isNaN(parsedAmountInput) || parsedAmountInput <= 0) {
      setError('Informe um valor numérico válido maior que zero.');
      return;
    }

    // Single transaction (or editing an existing single transaction)
    if (initialTransaction && initialTransaction.id) {
      onSave([{
        id: initialTransaction.id,
        description: description.trim(),
        amount: parsedAmountInput,
        type,
        category: type === 'transfer' ? 'Transferência Entre Contas' : (category || (type === 'income' ? 'Rendimentos' : 'Geral')),
        account: account || accounts[0]?.name || 'Conta Padrão',
        targetAccount: type === 'transfer' ? targetAccount : undefined,
        date,
        status,
        notes: notes.trim(),
        syncedWithSheet: isGoogleConnected && saveToSheet,
        isInstallment: launchMode === 'installment',
        installmentGroupId: initialTransaction.installmentGroupId,
        installmentCurrent: launchMode === 'installment' ? currentInstallment : undefined,
        installmentTotal: launchMode === 'installment' ? totalInstallments : undefined,
        isRecurring: launchMode === 'recurring',
        recurrenceGroupId: initialTransaction.recurrenceGroupId
      }], isGoogleConnected && saveToSheet);

      onClose();
      return;
    }

    // CREATE MULTIPLE TRANSACTIONS: Installment Mode (Works for Credit Cards AND Bank Accounts / Boletos / Carnês / Incomes)
    if (launchMode === 'installment' && type !== 'transfer') {
      const groupId = `inst-${Date.now()}`;
      const batch: (Omit<Transaction, 'id'> & { id?: string })[] = [];
      const installmentVal = parseFloat(calculatedPerInstallment.toFixed(2));

      // Clean base description (remove any previous (xx/yy) if user copied)
      const cleanDesc = description.replace(/\s*\(\d+\/\d+\)\s*$/, '').trim();

      for (let i = 0; i < remainingInstallmentsCount; i++) {
        const instNum = currentInstallment + i;
        const instDate = addMonthsToDate(date, i);
        const padLen = Math.max(2, String(totalInstallments).length);
        const padTotal = String(totalInstallments).padStart(padLen, '0');
        const padCurr = String(instNum).padStart(padLen, '0');

        batch.push({
          description: `${cleanDesc} (${padCurr}/${padTotal})`,
          amount: installmentVal,
          type,
          category: category || (type === 'income' ? 'Rendimentos & Dividendos' : 'Geral'),
          account: account || creditCards[0]?.name || accounts[0]?.name || 'Conta Corrente',
          date: instDate,
          status: i === 0 ? status : 'pending', // First installment has user status, future ones default to pending
          notes: notes ? `${notes} | Parcela ${instNum} de ${totalInstallments}` : `Parcela ${instNum} de ${totalInstallments}`,
          syncedWithSheet: isGoogleConnected && saveToSheet,
          isInstallment: true,
          installmentGroupId: groupId,
          installmentCurrent: instNum,
          installmentTotal: totalInstallments,
          originalAmount: calculatedTotalAmount
        });
      }

      onSave(batch, isGoogleConnected && saveToSheet);
      onClose();
      return;
    }

    // CREATE MULTIPLE TRANSACTIONS: Recurring Mode
    if (launchMode === 'recurring') {
      const groupId = `rec-${Date.now()}`;
      const batch: (Omit<Transaction, 'id'> & { id?: string })[] = [];
      const cleanDesc = description.trim();
      const val = parseFloat(parsedAmountInput.toFixed(2));

      if (recurrenceScope === 'full_year') {
        // Generate for all 12 months of the year specified in date (e.g. 2026-01 to 2026-12)
        const year = date.split('-')[0];
        const day = date.split('-')[2];
        for (let m = 0; m < 12; m++) {
          const mStr = String(m + 1).padStart(2, '0');
          const maxDay = new Date(parseInt(year, 10), m + 1, 0).getDate();
          const actualDay = String(Math.min(parseInt(day, 10), maxDay)).padStart(2, '0');
          const dStr = `${year}-${mStr}-${actualDay}`;
          const isPastOrCurrent = m <= currentMonthIdx;

          batch.push({
            description: cleanDesc,
            amount: val,
            type,
            category: type === 'transfer' ? 'Transferência Entre Contas' : (category || 'Geral'),
            account: account || accounts[0]?.name || 'Conta Padrão',
            targetAccount: type === 'transfer' ? targetAccount : undefined,
            date: dStr,
            status: isPastOrCurrent ? status : 'pending',
            notes: notes ? `${notes} (Fixo Mensal)` : 'Fixo Mensal',
            syncedWithSheet: isGoogleConnected && saveToSheet,
            isRecurring: true,
            recurrenceGroupId: groupId
          });
        }
      } else {
        // Generate from current date month forwards for recurringMonthsCount
        for (let i = 0; i < recurringMonthsCount; i++) {
          const recDate = addMonthsToDate(date, i);
          batch.push({
            description: cleanDesc,
            amount: val,
            type,
            category: type === 'transfer' ? 'Transferência Entre Contas' : (category || 'Geral'),
            account: account || accounts[0]?.name || 'Conta Padrão',
            targetAccount: type === 'transfer' ? targetAccount : undefined,
            date: recDate,
            status: i === 0 ? status : 'pending',
            notes: notes ? `${notes} (Fixo Mensal)` : 'Fixo Mensal',
            syncedWithSheet: isGoogleConnected && saveToSheet,
            isRecurring: true,
            recurrenceGroupId: groupId
          });
        }
      }

      onSave(batch, isGoogleConnected && saveToSheet);
      onClose();
      return;
    }

    // Default Single Transaction
    onSave([{
      description: description.trim(),
      amount: parsedAmountInput,
      type,
      category: type === 'transfer' ? 'Transferência Entre Contas' : (category || 'Geral'),
      account: account || accounts[0]?.name || 'Conta Padrão',
      targetAccount: type === 'transfer' ? targetAccount : undefined,
      date,
      status,
      notes: notes.trim(),
      syncedWithSheet: isGoogleConnected && saveToSheet,
      isInstallment: false,
      isRecurring: false
    }], isGoogleConnected && saveToSheet);

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div 
        id="transaction-form-modal-dialog"
        className="w-full max-w-lg bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[92vh]"
      >
        {/* Header */}
        <div className="p-4 sm:p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-white">
              {initialTransaction ? 'Editar Lançamento' : 'Novo Lançamento'}
            </h2>
            <p className="text-xs text-slate-500">
              {launchMode === 'installment' 
                ? 'Lançamento parcelado automático nos meses correspondentes' 
                : launchMode === 'recurring' 
                ? 'Lançamento fixo e recorrente para todos os meses' 
                : 'Lançamento pontual único'}
            </p>
          </div>
          <button
            id="close-transaction-modal-btn"
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-5 sm:p-6 space-y-4 overflow-y-auto">
          {error && (
            <div className="p-3 text-xs bg-rose-50 border border-rose-200 text-rose-700 dark:bg-rose-950/40 dark:border-rose-800 dark:text-rose-300 rounded-xl">
              {error}
            </div>
          )}

          {/* Type Selector Tabs */}
          <div className="grid grid-cols-3 gap-2 p-1 bg-slate-100 dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
            <button
              type="button"
              id="type-expense-btn"
              onClick={() => {
                setType('expense');
                const cat = categories.find(c => c.type === 'expense');
                if (cat) setCategory(cat.name);
              }}
              className={`flex items-center justify-center space-x-2 py-2 rounded-lg text-xs font-semibold transition ${
                type === 'expense'
                  ? 'bg-white dark:bg-slate-700 text-rose-600 dark:text-rose-400 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
              }`}
            >
              <ArrowDownCircle className="w-4 h-4" />
              <span>Despesa</span>
            </button>

            <button
              type="button"
              id="type-income-btn"
              onClick={() => {
                setType('income');
                const cat = categories.find(c => c.type === 'income');
                if (cat) setCategory(cat.name);
                if (launchMode === 'installment') setLaunchMode('single');
              }}
              className={`flex items-center justify-center space-x-2 py-2 rounded-lg text-xs font-semibold transition ${
                type === 'income'
                  ? 'bg-white dark:bg-slate-700 text-emerald-600 dark:text-emerald-400 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
              }`}
            >
              <ArrowUpCircle className="w-4 h-4" />
              <span>Receita</span>
            </button>

            <button
              type="button"
              id="type-transfer-btn"
              onClick={() => {
                setType('transfer');
                setLaunchMode('single');
              }}
              className={`flex items-center justify-center space-x-2 py-2 rounded-lg text-xs font-semibold transition ${
                type === 'transfer'
                  ? 'bg-white dark:bg-slate-700 text-blue-600 dark:text-blue-400 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
              }`}
            >
              <ArrowLeftRight className="w-4 h-4" />
              <span>Transferência</span>
            </button>
          </div>

          {/* Launch Mode Mode Switcher (Single vs Installment vs Recurring) */}
          {!initialTransaction && type !== 'transfer' && (
            <div className="space-y-1.5">
              <label className="block text-[11px] font-bold uppercase tracking-wider text-slate-500">
                Formato do Lançamento
              </label>
              <div className="grid grid-cols-3 gap-2">
                <button
                  type="button"
                  id="mode-single-btn"
                  onClick={() => setLaunchMode('single')}
                  className={`p-2.5 rounded-xl border text-xs font-semibold flex flex-col items-center justify-center space-y-1 transition ${
                    launchMode === 'single'
                      ? 'border-slate-900 bg-slate-900 text-white dark:border-white dark:bg-white dark:text-slate-900 shadow-sm'
                      : 'border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:border-slate-300'
                  }`}
                >
                  <DollarSign className="w-4 h-4" />
                  <span>À Vista / Único</span>
                </button>

                <button
                  type="button"
                  id="mode-installment-btn"
                  onClick={() => setLaunchMode('installment')}
                  className={`p-2.5 rounded-xl border text-xs font-semibold flex flex-col items-center justify-center space-y-1 transition ${
                    launchMode === 'installment'
                      ? 'border-indigo-600 bg-indigo-600 text-white shadow-sm shadow-indigo-600/30'
                      : 'border-indigo-200 dark:border-indigo-800/80 bg-indigo-50/50 dark:bg-indigo-950/30 text-indigo-700 dark:text-indigo-300 hover:bg-indigo-50'
                  }`}
                >
                  <Layers className="w-4 h-4" />
                  <span>Parcelado</span>
                  <span className={`text-[9px] font-normal leading-none ${launchMode === 'installment' ? 'text-indigo-100' : 'text-slate-500 dark:text-slate-400'}`}>
                    Cartão / Carnê / Conta
                  </span>
                </button>

                <button
                  type="button"
                  id="mode-recurring-btn"
                  onClick={() => setLaunchMode('recurring')}
                  className={`p-2.5 rounded-xl border text-xs font-semibold flex flex-col items-center justify-center space-y-1 transition ${
                    launchMode === 'recurring'
                      ? 'border-emerald-600 bg-emerald-600 text-white shadow-sm shadow-emerald-600/30'
                      : 'border-emerald-200 dark:border-emerald-800/80 bg-emerald-50/50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-300 hover:bg-emerald-50'
                  }`}
                >
                  <Repeat className="w-4 h-4" />
                  <span>Fixo Todo Mês</span>
                  <span className={`text-[9px] font-normal leading-none ${launchMode === 'recurring' ? 'text-emerald-100' : 'text-slate-500 dark:text-slate-400'}`}>
                    Assinatura / Salário
                  </span>
                </button>
              </div>
            </div>
          )}

          {/* Amount & Date */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                {launchMode === 'installment' 
                  ? (installmentValueMode === 'per_installment' ? 'Valor de Cada Parcela (R$) *' : (type === 'income' ? 'Valor Total a Receber (R$) *' : 'Valor Total da Compra / Conta (R$) *'))
                  : 'Valor (R$) *'}
              </label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-bold text-slate-400">R$</span>
                <input
                  id="transaction-amount-input"
                  type="number"
                  step="0.01"
                  placeholder="0,00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  required
                  className="w-full pl-9 pr-3.5 py-2.5 text-sm font-bold rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                {launchMode === 'installment' ? 'Data da 1ª Parcela *' : launchMode === 'recurring' ? 'Data Base / Dia do Mês *' : 'Data *'}
              </label>
              <div className="relative">
                <input
                  id="transaction-date-input"
                  type="date"
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  required
                  className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>
            </div>
          </div>

          {/* INSTALLMENT SECTION SETTINGS */}
          {launchMode === 'installment' && (
            <div className="p-4 rounded-2xl bg-indigo-50/80 dark:bg-indigo-950/40 border border-indigo-200 dark:border-indigo-800/60 space-y-3 animate-fade-in">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2 text-indigo-900 dark:text-indigo-200 font-bold text-xs">
                  <Layers className="w-4 h-4 text-indigo-600" />
                  <span>Configurações do Parcelamento ({type === 'income' ? 'Receita Parcelada' : 'Despesa Parcelada'})</span>
                </div>

                {/* Switch between value per installment vs total */}
                <div className="flex items-center bg-white dark:bg-slate-800 rounded-lg p-0.5 border border-indigo-200 dark:border-indigo-800 text-[11px]">
                  <button
                    type="button"
                    onClick={() => setInstallmentValueMode('per_installment')}
                    className={`px-2 py-0.5 rounded-md font-semibold transition ${
                      installmentValueMode === 'per_installment'
                        ? 'bg-indigo-600 text-white'
                        : 'text-slate-600 dark:text-slate-300'
                    }`}
                  >
                    Por Parcela
                  </button>
                  <button
                    type="button"
                    onClick={() => setInstallmentValueMode('total')}
                    className={`px-2 py-0.5 rounded-md font-semibold transition ${
                      installmentValueMode === 'total'
                        ? 'bg-indigo-600 text-white'
                        : 'text-slate-600 dark:text-slate-300'
                    }`}
                  >
                    Valor Total
                  </button>
                </div>
              </div>

              {/* Installment count selectors */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    {type === 'income' ? 'Total de Parcelas a Receber *' : 'Total de Parcelas (Carnê/Cartão/Conta) *'}
                  </label>
                  <input
                    type="number"
                    min="2"
                    max="200"
                    value={totalInstallments}
                    onChange={(e) => setTotalInstallments(Math.min(200, Math.max(2, parseInt(e.target.value, 10) || 2)))}
                    className="w-full px-3 py-1.5 text-xs font-bold rounded-xl border border-indigo-200 dark:border-indigo-800 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                  />
                  {/* Quick installment chips */}
                  <div className="flex items-center space-x-1 mt-1.5 flex-wrap gap-1">
                    {[2, 3, 6, 10, 12, 18, 24, 36, 48, 60, 120, 180, 200].map(n => (
                      <button
                        key={n}
                        type="button"
                        onClick={() => setTotalInstallments(n)}
                        className={`px-1.5 py-0.5 text-[10px] rounded font-semibold transition ${
                          totalInstallments === n 
                            ? 'bg-indigo-600 text-white' 
                            : 'bg-white dark:bg-slate-800 text-indigo-600 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800'
                        }`}
                      >
                        {n}x
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Qual parcela lançar agora? *
                  </label>
                  <input
                    type="number"
                    min="1"
                    max={totalInstallments}
                    value={currentInstallment}
                    onChange={(e) => setCurrentInstallment(Math.min(totalInstallments, Math.max(1, parseInt(e.target.value, 10) || 1)))}
                    className="w-full px-3 py-1.5 text-xs font-bold rounded-xl border border-indigo-200 dark:border-indigo-800 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                  />
                  <span className="text-[10px] text-slate-500 block mt-1">
                    {currentInstallment === 1 
                      ? 'Lançamento novo: lança todas as parcelas' 
                      : `Faltam ${remainingInstallmentsCount} parcelas (da ${currentInstallment} até ${totalInstallments})`}
                  </span>
                </div>
              </div>

              {/* Installment Summary Card */}
              <div className="p-3 bg-white dark:bg-slate-900 rounded-xl border border-indigo-200 dark:border-indigo-800/80 text-xs space-y-1.5">
                <div className="flex items-center justify-between text-indigo-950 dark:text-indigo-200 font-bold">
                  <span>Resumo do Parcelamento:</span>
                  <span className="text-indigo-600 dark:text-indigo-400 text-sm">
                    {remainingInstallmentsCount}x de R$ {calculatedPerInstallment.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </span>
                </div>
                <div className="flex items-center justify-between text-[11px] text-slate-600 dark:text-slate-400">
                  <span>{type === 'income' ? 'Total a Receber:' : 'Valor Total do Parcelamento:'}</span>
                  <span className="font-semibold text-slate-800 dark:text-slate-200">
                    R$ {calculatedTotalAmount.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </span>
                </div>
                <div className="flex items-center justify-between text-[11px] text-slate-600 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-slate-800">
                  <span>Período dos Lançamentos:</span>
                  <span className="font-semibold text-indigo-700 dark:text-indigo-300">
                    {date.split('-').reverse().join('/')} até {addMonthsToDate(date, remainingInstallmentsCount - 1).split('-').reverse().join('/')}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* RECURRING SECTION SETTINGS */}
          {launchMode === 'recurring' && (
            <div className="p-4 rounded-2xl bg-emerald-50/80 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/60 space-y-3 animate-fade-in">
              <div className="flex items-center space-x-2 text-emerald-900 dark:text-emerald-200 font-bold text-xs">
                <Repeat className="w-4 h-4 text-emerald-600" />
                <span>Configurações do Lançamento Fixo Mensal</span>
              </div>

              <div className="space-y-2">
                <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300">
                  Frequência de Replicação:
                </label>
                
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                  <button
                    type="button"
                    onClick={() => setRecurrenceScope('end_of_year')}
                    className={`p-2 rounded-xl border text-left text-xs font-semibold transition ${
                      recurrenceScope === 'end_of_year'
                        ? 'border-emerald-600 bg-emerald-600 text-white'
                        : 'border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300'
                    }`}
                  >
                    <span>Até Fim do Ano</span>
                    <span className="block text-[10px] font-normal opacity-80">
                      {12 - currentMonthIdx} meses restantes
                    </span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setRecurrenceScope('full_year')}
                    className={`p-2 rounded-xl border text-left text-xs font-semibold transition ${
                      recurrenceScope === 'full_year'
                        ? 'border-emerald-600 bg-emerald-600 text-white'
                        : 'border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300'
                    }`}
                  >
                    <span>Ano Completo</span>
                    <span className="block text-[10px] font-normal opacity-80">
                      12 meses de {date.split('-')[0]}
                    </span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setRecurrenceScope('custom')}
                    className={`p-2 rounded-xl border text-left text-xs font-semibold transition ${
                      recurrenceScope === 'custom'
                        ? 'border-emerald-600 bg-emerald-600 text-white'
                        : 'border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300'
                    }`}
                  >
                    <span>Personalizado</span>
                    <span className="block text-[10px] font-normal opacity-80">
                      Definir quantidade
                    </span>
                  </button>
                </div>

                {recurrenceScope === 'custom' && (
                  <div className="pt-2 flex items-center space-x-2">
                    <span className="text-xs text-slate-600 dark:text-slate-400">Repetir por:</span>
                    <input
                      type="number"
                      min="2"
                      max="60"
                      value={recurrenceCustomMonths}
                      onChange={(e) => setRecurrenceCustomMonths(Math.max(2, parseInt(e.target.value, 10) || 2))}
                      className="w-20 px-2 py-1 text-xs font-bold rounded-lg border border-emerald-300 dark:border-emerald-700 bg-white dark:bg-slate-800"
                    />
                    <span className="text-xs text-slate-600 dark:text-slate-400">meses subsequentes</span>
                  </div>
                )}
              </div>

              {/* Recurring summary card */}
              <div className="p-3 bg-white dark:bg-slate-900 rounded-xl border border-emerald-200 dark:border-emerald-800/80 text-xs space-y-1">
                <div className="flex items-center justify-between text-emerald-950 dark:text-emerald-200 font-bold">
                  <span>Resumo da Recorrência:</span>
                  <span className="text-emerald-600 dark:text-emerald-400 font-bold">
                    {recurringMonthsCount} lançamentos automáticos
                  </span>
                </div>
                <p className="text-[11px] text-slate-600 dark:text-slate-400">
                  Será criado um lançamento fixo de <strong>R$ {parsedAmountInput.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> todo dia <strong>{date.split('-')[2]}</strong> nos meses correspondentes.
                </p>
              </div>
            </div>
          )}

          {/* Description */}
          <div>
            <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
              Descrição do Lançamento *
            </label>
            <input
              id="transaction-desc-input"
              type="text"
              placeholder={launchMode === 'installment' ? "Ex: Smart TV Samsung 55', iPhone, Notebook..." : launchMode === 'recurring' ? "Ex: Aluguel, Academia Smart Fit, Internet Fibra..." : "Ex: Supermercado Pão de Açúcar, Salário..."}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            />
          </div>

          {/* Category (if not transfer) */}
          {type !== 'transfer' && (
            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300">
                  Categoria
                </label>
                {onOpenCreateCategory && (
                  <button
                    type="button"
                    onClick={() => onOpenCreateCategory(type === 'income' ? 'income' : 'expense')}
                    className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400 hover:underline flex items-center space-x-1"
                  >
                    <span>+ Nova Categoria</span>
                  </button>
                )}
              </div>
              <select
                id="transaction-category-select"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none font-medium"
              >
                {filteredCategories.map((c) => (
                  <option key={c.id} value={c.name}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Account / Card */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                {type === 'transfer' ? 'Conta de Origem' : 'Conta / Cartão'}
              </label>
              <select
                id="transaction-account-select"
                value={account}
                onChange={(e) => setAccount(e.target.value)}
                className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
              >
                {type !== 'transfer' && (
                  <optgroup label="Cartões de Crédito">
                    {creditCards.map(card => (
                      <option key={card.id} value={card.name}>
                        💳 {card.name} (Fatura: R$ {card.currentInvoice.toLocaleString('pt-BR', { minimumFractionDigits: 2 })})
                      </option>
                    ))}
                  </optgroup>
                )}
                <optgroup label="Contas Bancárias">
                  {accounts.map(acc => (
                    <option key={acc.id} value={acc.name}>
                      🏛️ {acc.name} (R$ {acc.balance.toLocaleString('pt-BR', { minimumFractionDigits: 2 })})
                    </option>
                  ))}
                </optgroup>
              </select>
            </div>

            {type === 'transfer' ? (
              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                  Conta de Destino
                </label>
                <select
                  id="transaction-target-account-select"
                  value={targetAccount}
                  onChange={(e) => setTargetAccount(e.target.value)}
                  className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                >
                  {accounts.map(acc => (
                    <option key={acc.id} value={acc.name}>
                      {acc.name}
                    </option>
                  ))}
                </select>
              </div>
            ) : (
              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                  Status de Pagamento
                </label>
                <select
                  id="transaction-status-select"
                  value={status}
                  onChange={(e) => setStatus(e.target.value as 'completed' | 'pending')}
                  className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                >
                  <option value="completed">Concluído / Pago</option>
                  <option value="pending">Pendente / Agendado</option>
                </select>
              </div>
            )}
          </div>

          {/* Credit Card Automatic Invoice Banner */}
          {selectedCard && type === 'expense' && (
            <div className="p-3 bg-indigo-50/90 dark:bg-indigo-950/40 rounded-xl border border-indigo-200 dark:border-indigo-800/70 text-xs space-y-1.5 animate-fade-in">
              <div className="flex items-center justify-between text-indigo-900 dark:text-indigo-200 font-bold">
                <span className="flex items-center space-x-1.5">
                  <CardIcon className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                  <span>Lançamento Automático na Fatura do Cartão</span>
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-indigo-100 dark:bg-indigo-900 text-indigo-700 dark:text-indigo-300 font-semibold border border-indigo-200 dark:border-indigo-800">
                  Fecha dia {selectedCard.closingDay} • Vence dia {selectedCard.dueDay}
                </span>
              </div>
              <p className="text-[11px] text-slate-600 dark:text-slate-300 leading-relaxed">
                {launchMode === 'recurring' ? (
                  <>
                    🔄 <strong>Lançamento Fixo na Fatura:</strong> O valor de <strong>R$ {parsedAmountInput.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> será lançado automaticamente na fatura do <strong>{selectedCard.name}</strong> em todos os <strong>{recurringMonthsCount} meses</strong> gerados.
                  </>
                ) : launchMode === 'installment' ? (
                  <>
                    💳 <strong>Parcelamento no Cartão:</strong> Cada uma das <strong>{remainingInstallmentsCount} parcelas de R$ {calculatedPerInstallment.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> será lançada mês a mês na fatura correspondente.
                  </>
                ) : (
                  <>
                    ⚡ <strong>Fatura do Mês:</strong> Este valor de <strong>R$ {parsedAmountInput.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> será computado na fatura ativa do cartão <strong>{selectedCard.name}</strong>.
                  </>
                )}
              </p>
            </div>
          )}

          {/* Non-Card Account Installment / Recurring Banner (Boleto, Carnê, Empréstimo, Conta Bancária, Receitas) */}
          {!selectedCard && type !== 'transfer' && (launchMode === 'installment' || launchMode === 'recurring') && (
            <div className={`p-3 rounded-xl border text-xs space-y-1.5 animate-fade-in ${
              launchMode === 'installment'
                ? 'bg-blue-50/90 dark:bg-blue-950/40 border-blue-200 dark:border-blue-800/70 text-blue-900 dark:text-blue-200'
                : 'bg-emerald-50/90 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-800/70 text-emerald-900 dark:text-emerald-200'
            }`}>
              <div className="flex items-center justify-between font-bold">
                <span className="flex items-center space-x-1.5">
                  {launchMode === 'installment' ? <Layers className="w-4 h-4 text-blue-600 dark:text-blue-400" /> : <Repeat className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />}
                  <span>
                    {launchMode === 'installment' 
                      ? (type === 'income' ? 'Recebimento Parcelado em Conta' : 'Parcelamento em Conta / Carnê / Boleto / Empréstimo') 
                      : (type === 'income' ? 'Receita Recorrente em Conta' : 'Débito / Despesa Fixa em Conta')}
                  </span>
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/80 dark:bg-slate-800 font-semibold border">
                  Dia {date.split('-')[2] || '01'} de cada mês
                </span>
              </div>
              <p className="text-[11px] text-slate-600 dark:text-slate-300 leading-relaxed">
                {launchMode === 'installment' ? (
                  type === 'income' ? (
                    <>
                      💰 <strong>Entrada Parcelada:</strong> Serão agendadas <strong>{remainingInstallmentsCount} parcelas de R$ {calculatedPerInstallment.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> a serem creditadas na conta <strong>{account || 'Conta Corrente'}</strong> mês a mês.
                    </>
                  ) : (
                    <>
                      📑 <strong>Parcelamento Direto:</strong> Serão agendadas <strong>{remainingInstallmentsCount} parcelas de R$ {calculatedPerInstallment.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> (totalizando <strong>R$ {calculatedTotalAmount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong>) na conta <strong>{account || 'Conta Corrente'}</strong>.
                    </>
                  )
                ) : (
                  type === 'income' ? (
                    <>
                      🔄 <strong>Receita Fixa:</strong> O valor de <strong>R$ {parsedAmountInput.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> será creditado todo mês na conta <strong>{account || 'Conta Corrente'}</strong> nos <strong>{recurringMonthsCount} meses</strong> selecionados.
                    </>
                  ) : (
                    <>
                      🔄 <strong>Despesa Fixa:</strong> O valor de <strong>R$ {parsedAmountInput.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong> será agendado todo mês na conta <strong>{account || 'Conta Corrente'}</strong> nos <strong>{recurringMonthsCount} meses</strong> selecionados.
                    </>
                  )
                )}
              </p>
            </div>
          )}

          {/* Notes */}
          <div>
            <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
              Observações (Opcional)
            </label>
            <input
              id="transaction-notes-input"
              type="text"
              placeholder="Ex: Comprovante arquivado, garantia de 1 ano..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="w-full px-3.5 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            />
          </div>

          {/* Direct Sheets Sync checkbox */}
          {isGoogleConnected && (
            <div className="p-3 bg-emerald-50 dark:bg-emerald-950/30 rounded-xl border border-emerald-200 dark:border-emerald-800 flex items-center justify-between">
              <div className="flex items-center space-x-2 text-xs text-emerald-800 dark:text-emerald-300 font-medium">
                <FileSpreadsheet className="w-4 h-4 text-emerald-600" />
                <span>Salvar linha na Planilha Google</span>
              </div>
              <input
                id="sync-to-sheet-checkbox"
                type="checkbox"
                checked={saveToSheet}
                onChange={(e) => setSaveToSheet(e.target.checked)}
                className="w-4 h-4 text-emerald-600 rounded focus:ring-emerald-500"
              />
            </div>
          )}

          {/* Action buttons */}
          <div className="flex items-center justify-end space-x-2 pt-3 border-t border-slate-200 dark:border-slate-800">
            <button
              type="button"
              id="cancel-transaction-modal-btn"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition"
            >
              Cancelar
            </button>
            <button
              type="submit"
              id="save-transaction-modal-btn"
              className={`px-5 py-2 text-xs font-semibold rounded-xl text-white shadow-sm transition flex items-center space-x-1.5 ${
                launchMode === 'installment'
                  ? 'bg-indigo-600 hover:bg-indigo-700 shadow-indigo-600/20'
                  : 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-600/20'
              }`}
            >
              <Check className="w-4 h-4" />
              <span>
                {initialTransaction 
                  ? 'Salvar Alterações' 
                  : launchMode === 'installment' 
                  ? `Lançar ${remainingInstallmentsCount} Parcelas` 
                  : launchMode === 'recurring' 
                  ? `Lançar ${recurringMonthsCount} Meses` 
                  : 'Adicionar Lançamento'}
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
