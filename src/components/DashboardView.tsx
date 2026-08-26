import React from 'react';
import { 
  TrendingUp, 
  TrendingDown, 
  Wallet, 
  PiggyBank, 
  ArrowUpRight, 
  ArrowDownRight, 
  AlertCircle, 
  CheckCircle2, 
  Clock, 
  CreditCard as CardIcon,
  ChevronRight,
  Plus
} from 'lucide-react';
import { 
  ResponsiveContainer, 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  Tooltip, 
  PieChart, 
  Pie, 
  Cell, 
  BarChart, 
  Bar 
} from 'recharts';
import { Account, Category, CreditCard, FinancialGoal, Transaction } from '../types';
import { MONTH_NAMES } from '../data/initialData';

interface DashboardViewProps {
  selectedMonth: number;
  selectedYear: number;
  transactions: Transaction[];
  categories: Category[];
  accounts: Account[];
  creditCards: CreditCard[];
  goals: FinancialGoal[];
  onOpenNewTransaction: () => void;
  onOpenTransactionsTab: () => void;
  onToggleTransactionStatus: (id: string) => void;
}

const COLORS = ['#10B981', '#3B82F6', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#06B6D4', '#64748B'];

export const DashboardView: React.FC<DashboardViewProps> = ({
  selectedMonth,
  selectedYear,
  transactions,
  categories,
  accounts,
  creditCards,
  goals,
  onOpenNewTransaction,
  onOpenTransactionsTab,
  onToggleTransactionStatus
}) => {
  // Filter transactions for current month
  const currentMonthPrefix = `${selectedYear}-${String(selectedMonth + 1).padStart(2, '0')}`;
  const monthTransactions = transactions.filter(t => t.date.startsWith(currentMonthPrefix));

  const totalIncome = monthTransactions
    .filter(t => t.type === 'income')
    .reduce((sum, t) => sum + t.amount, 0);

  const totalExpense = monthTransactions
    .filter(t => t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0);

  const monthlyBalance = totalIncome - totalExpense;
  const savingsRate = totalIncome > 0 ? Math.max(0, Math.round(((totalIncome - totalExpense) / totalIncome) * 100)) : 0;

  const totalBankBalance = accounts.reduce((sum, a) => sum + a.balance, 0);
  const totalCardsInvoice = creditCards.reduce((sum, c) => sum + c.currentInvoice, 0);

  const pendingExpenses = monthTransactions
    .filter(t => t.type === 'expense' && t.status === 'pending')
    .reduce((sum, t) => sum + t.amount, 0);

  // Category breakdown data
  const expenseByCategory = categories
    .filter(c => c.type === 'expense')
    .map(c => {
      const spent = monthTransactions
        .filter(t => t.type === 'expense' && t.category === c.name)
        .reduce((sum, t) => sum + t.amount, 0);
      return {
        name: c.name,
        value: spent,
        budget: c.monthlyBudget,
        color: c.color
      };
    })
    .filter(c => c.value > 0);

  // If no expenses this month, add placeholder for pie chart
  const pieData = expenseByCategory.length > 0 ? expenseByCategory : [
    { name: 'Nenhuma Despesa Registrada', value: 1, color: '#CBD5E1', budget: 0 }
  ];

  // Daily Cashflow Trend for current month
  const daysInMonth = new Date(selectedYear, selectedMonth + 1, 0).getDate();
  const dailyData = Array.from({ length: daysInMonth }, (_, i) => {
    const day = i + 1;
    const dayStr = `${currentMonthPrefix}-${String(day).padStart(2, '0')}`;
    const dayIncome = monthTransactions
      .filter(t => t.date === dayStr && t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);
    const dayExpense = monthTransactions
      .filter(t => t.date === dayStr && t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);
    return {
      day: `${day}`,
      Receitas: dayIncome,
      Despesas: dayExpense
    };
  });

  // Recent 6 transactions
  const recentTransactions = [...monthTransactions].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()).slice(0, 6);

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card: Saldo Geral Acumulado */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Patrimônio em Contas
            </span>
            <div className="p-2 rounded-xl bg-blue-50 text-blue-600 dark:bg-blue-950/50 dark:text-blue-400">
              <Wallet className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-slate-900 dark:text-white">
            R$ {totalBankBalance.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-slate-800">
            <span>{accounts.length} contas bancárias</span>
            <span className="text-emerald-600 font-medium">Líquido</span>
          </div>
        </div>

        {/* Card: Receitas do Mês */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Receitas ({MONTH_NAMES[selectedMonth]})
            </span>
            <div className="p-2 rounded-xl bg-emerald-50 text-emerald-600 dark:bg-emerald-950/50 dark:text-emerald-400">
              <ArrowUpRight className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-emerald-600 dark:text-emerald-400">
            R$ {totalIncome.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-slate-800">
            <span>Entradas no período</span>
            <span className="text-emerald-600 font-medium">Ativo</span>
          </div>
        </div>

        {/* Card: Despesas do Mês */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Despesas ({MONTH_NAMES[selectedMonth]})
            </span>
            <div className="p-2 rounded-xl bg-rose-50 text-rose-600 dark:bg-rose-950/50 dark:text-rose-400">
              <ArrowDownRight className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-rose-600 dark:text-rose-400">
            R$ {totalExpense.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-slate-800">
            <span>{pendingExpenses > 0 ? `R$ ${pendingExpenses.toFixed(2)} pendente` : '100% quitado'}</span>
            <span className="text-rose-500 font-medium">{pendingExpenses > 0 ? 'A Vencer' : 'Ok'}</span>
          </div>
        </div>

        {/* Card: Balanço / Economia */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Balanço do Mês
            </span>
            <div className={`p-2 rounded-xl ${monthlyBalance >= 0 ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/50' : 'bg-rose-50 text-rose-600 dark:bg-rose-950/50'}`}>
              <PiggyBank className="w-4 h-4" />
            </div>
          </div>
          <div className={`text-2xl font-black ${monthlyBalance >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400'}`}>
            R$ {monthlyBalance.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
          <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-slate-800">
            <span>Taxa de Poupança</span>
            <span className="font-semibold text-emerald-600">{savingsRate}% da renda</span>
          </div>
        </div>
      </div>

      {/* Main Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Evolution Chart (2 cols) */}
        <div className="lg:col-span-2 p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Fluxo de Caixa Diário ({MONTH_NAMES[selectedMonth]} {selectedYear})
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Distribuição de entradas e saídas ao longo dos dias
              </p>
            </div>
            <div className="flex items-center space-x-3 text-xs">
              <div className="flex items-center space-x-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
                <span className="text-slate-600 dark:text-slate-400">Receitas</span>
              </div>
              <div className="flex items-center space-x-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-rose-500"></span>
                <span className="text-slate-600 dark:text-slate-400">Despesas</span>
              </div>
            </div>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={dailyData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="incomeGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10B981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10B981" stopOpacity={0.0} />
                  </linearGradient>
                  <linearGradient id="expenseGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#EF4444" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#EF4444" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="day" tickLine={false} tick={{ fontSize: 11, fill: '#94A3B8' }} />
                <YAxis tickLine={false} tick={{ fontSize: 11, fill: '#94A3B8' }} />
                <Tooltip 
                  formatter={(value: any) => [`R$ ${Number(value || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`, '']}
                  contentStyle={{ backgroundColor: '#1E293B', borderRadius: '12px', border: 'none', color: '#fff', fontSize: '12px' }}
                />
                <Area type="monotone" dataKey="Receitas" stroke="#10B981" strokeWidth={2} fillOpacity={1} fill="url(#incomeGradient)" />
                <Area type="monotone" dataKey="Despesas" stroke="#EF4444" strokeWidth={2} fillOpacity={1} fill="url(#expenseGradient)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Category Breakdown Donut (1 col) */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4 flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Despesas por Categoria
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Onde o seu dinheiro foi investido este mês
            </p>
          </div>

          <div className="h-48 w-full flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={75}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color || COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip 
                  formatter={(value: any) => [`R$ ${Number(value || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`, 'Valor']}
                  contentStyle={{ backgroundColor: '#1E293B', borderRadius: '12px', border: 'none', color: '#fff', fontSize: '12px' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>

          {/* Legend */}
          <div className="space-y-1.5 max-h-36 overflow-y-auto pr-1">
            {expenseByCategory.slice(0, 4).map((item, idx) => (
              <div key={idx} className="flex items-center justify-between text-xs">
                <div className="flex items-center space-x-2 truncate">
                  <span className="w-2 h-2 rounded-full flex-shrink-0" style={{ backgroundColor: item.color || COLORS[idx] }}></span>
                  <span className="text-slate-700 dark:text-slate-300 truncate">{item.name}</span>
                </div>
                <span className="font-semibold text-slate-900 dark:text-white ml-2 flex-shrink-0">
                  R$ {item.value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Bottom Section: Recent Transactions & Financial Goals */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Transactions (2 cols) */}
        <div className="lg:col-span-2 p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Últimos Lançamentos
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Atividades recentes registradas em {MONTH_NAMES[selectedMonth]}
              </p>
            </div>
            <button
              id="view-all-transactions-btn"
              onClick={onOpenTransactionsTab}
              className="flex items-center space-x-1 text-xs font-semibold text-emerald-600 dark:text-emerald-400 hover:underline"
            >
              <span>Ver todos</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="space-y-2">
            {recentTransactions.length === 0 ? (
              <div className="text-center py-8 text-slate-400 text-xs space-y-2">
                <p>Nenhum lançamento registrado neste mês.</p>
                <button
                  id="dashboard-add-first-btn"
                  onClick={onOpenNewTransaction}
                  className="px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-xs font-medium inline-flex items-center space-x-1"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Cadastrar Lançamento</span>
                </button>
              </div>
            ) : (
              recentTransactions.map((t) => (
                <div
                  key={t.id}
                  className="p-3 rounded-xl border border-slate-100 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/40 transition flex items-center justify-between gap-3"
                >
                  <div className="flex items-center space-x-3 min-w-0">
                    <div className={`p-2 rounded-xl flex-shrink-0 ${
                      t.type === 'income'
                        ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/50'
                        : 'bg-rose-50 text-rose-600 dark:bg-rose-950/50'
                    }`}>
                      {t.type === 'income' ? <ArrowUpRight className="w-4 h-4" /> : <ArrowDownRight className="w-4 h-4" />}
                    </div>
                    <div className="truncate">
                      <p className="text-xs font-bold text-slate-900 dark:text-white truncate">
                        {t.description}
                      </p>
                      <div className="flex items-center space-x-2 text-[11px] text-slate-500">
                        <span>{t.date.split('-').reverse().join('/')}</span>
                        <span>•</span>
                        <span className="truncate">{t.category}</span>
                        <span>•</span>
                        <span className="truncate">{t.account}</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center space-x-3 flex-shrink-0">
                    <span className={`text-xs font-black ${
                      t.type === 'income' ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-900 dark:text-white'
                    }`}>
                      {t.type === 'income' ? '+' : '-'} R$ {t.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>

                    <button
                      onClick={() => onToggleTransactionStatus(t.id)}
                      className={`p-1 rounded-lg text-xs transition ${
                        t.status === 'completed'
                          ? 'text-emerald-600 bg-emerald-50 dark:bg-emerald-950/50'
                          : 'text-amber-500 bg-amber-50 dark:bg-amber-950/50'
                      }`}
                      title={t.status === 'completed' ? 'Status: Pago' : 'Status: Pendente (Clique para marcar como pago)'}
                    >
                      {t.status === 'completed' ? (
                        <CheckCircle2 className="w-4 h-4" />
                      ) : (
                        <Clock className="w-4 h-4" />
                      )}
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Financial Goals Widget (1 col) */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Metas & Objetivos 2026
            </h3>
            <span className="text-[11px] font-semibold text-emerald-600 bg-emerald-50 dark:bg-emerald-950/50 px-2 py-0.5 rounded-full">
              {goals.length} ativas
            </span>
          </div>

          <div className="space-y-3">
            {goals.map((g) => {
              const progress = Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100));
              return (
                <div key={g.id} className="p-3.5 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/20 space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-bold text-slate-800 dark:text-slate-200 truncate pr-2">
                      {g.title}
                    </span>
                    <span className="font-black text-emerald-600 dark:text-emerald-400 flex-shrink-0">
                      {progress}%
                    </span>
                  </div>

                  {/* Progress bar */}
                  <div className="w-full h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-500"
                      style={{
                        width: `${progress}%`,
                        backgroundColor: g.color || '#10B981'
                      }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-slate-500">
                    <span>R$ {g.currentAmount.toLocaleString('pt-BR')} acumulados</span>
                    <span>Meta: R$ {g.targetAmount.toLocaleString('pt-BR')}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
