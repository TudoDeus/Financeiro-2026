import React from 'react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  Tooltip, 
  ResponsiveContainer, 
  Legend, 
  LineChart, 
  Line, 
  CartesianGrid 
} from 'recharts';
import { 
  BarChart3, 
  TrendingUp, 
  TrendingDown, 
  PieChart, 
  CreditCard, 
  ShieldCheck, 
  Award,
  Sparkles
} from 'lucide-react';
import { Category, Transaction } from '../types';
import { MONTH_NAMES } from '../data/initialData';

interface ReportsViewProps {
  transactions: Transaction[];
  categories: Category[];
  selectedYear: number;
}

export const ReportsView: React.FC<ReportsViewProps> = ({
  transactions,
  categories,
  selectedYear
}) => {
  // Annual breakdown for all 12 months of 2026
  const annualData = MONTH_NAMES.map((name, index) => {
    const monthPrefix = `${selectedYear}-${String(index + 1).padStart(2, '0')}`;
    const monthTx = transactions.filter(t => t.date.startsWith(monthPrefix));

    const income = monthTx
      .filter(t => t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);

    const expense = monthTx
      .filter(t => t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);

    const balance = income - expense;

    return {
      name: name.substring(0, 3),
      fullName: name,
      Receitas: income,
      Despesas: expense,
      Saldo: balance
    };
  });

  const totalYearIncome = annualData.reduce((sum, d) => sum + d.Receitas, 0);
  const totalYearExpense = annualData.reduce((sum, d) => sum + d.Despesas, 0);
  const totalYearBalance = totalYearIncome - totalYearExpense;
  const averageMonthlyExpense = totalYearExpense / 12;

  // Expenses by category ranking (year total)
  const categoryRankings = categories
    .filter(c => c.type === 'expense')
    .map(c => {
      const totalSpent = transactions
        .filter(t => t.type === 'expense' && t.category === c.name && t.date.startsWith(`${selectedYear}`))
        .reduce((sum, t) => sum + t.amount, 0);
      return {
        name: c.name,
        color: c.color,
        amount: totalSpent,
        percentage: totalYearExpense > 0 ? Math.round((totalSpent / totalYearExpense) * 100) : 0
      };
    })
    .filter(c => c.amount > 0)
    .sort((a, b) => b.amount - a.amount);

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Top Annual Stats Banner */}
      <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center space-x-2">
              <BarChart3 className="w-5 h-5 text-emerald-600" />
              <h2 className="text-base font-bold text-slate-900 dark:text-white">
                Demonstrativo Financeiro Anual ({selectedYear})
              </h2>
            </div>
            <p className="text-xs text-slate-500">
              Evolução e consolidação dos resultados de Janeiro a Dezembro
            </p>
          </div>

          <div className="flex items-center space-x-2 text-xs">
            <span className="px-3 py-1.5 rounded-xl bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 font-semibold border border-emerald-200 dark:border-emerald-800">
              Ano Completo 2026
            </span>
          </div>
        </div>

        {/* 4 Annual Metric boxes */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 pt-2">
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
            <span className="text-[11px] font-semibold text-slate-500 uppercase">Receitas Totais</span>
            <p className="text-xl font-black text-emerald-600 dark:text-emerald-400">
              R$ {totalYearIncome.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
            <span className="text-[11px] font-semibold text-slate-500 uppercase">Despesas Totais</span>
            <p className="text-xl font-black text-rose-600 dark:text-rose-400">
              R$ {totalYearExpense.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
            <span className="text-[11px] font-semibold text-slate-500 uppercase">Superávit / Saldo Líquido</span>
            <p className={`text-xl font-black ${totalYearBalance >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
              R$ {totalYearBalance.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">
            <span className="text-[11px] font-semibold text-slate-500 uppercase">Média Mensal de Gastos</span>
            <p className="text-xl font-black text-slate-900 dark:text-white">
              R$ {averageMonthlyExpense.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
          </div>
        </div>
      </div>

      {/* Annual Income vs Expense Comparison Bar Chart */}
      <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div>
          <h3 className="text-sm font-bold text-slate-900 dark:text-white">
            Comparativo Mensal: Receitas vs Despesas ({selectedYear})
          </h3>
          <p className="text-xs text-slate-500">
            Acompanhe a relação entre ganhos e custos em cada mês do ano
          </p>
        </div>

        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={annualData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#64748B' }} />
              <YAxis tick={{ fontSize: 11, fill: '#64748B' }} />
              <Tooltip
                formatter={(value: any) => [`R$ ${Number(value || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`, '']}
                contentStyle={{ backgroundColor: '#1E293B', borderRadius: '12px', border: 'none', color: '#fff', fontSize: '12px' }}
              />
              <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }} />
              <Bar dataKey="Receitas" fill="#10B981" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Despesas" fill="#EF4444" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Category Spend Ranking Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Ranking de Gastos por Categoria ({selectedYear})
            </h3>
            <p className="text-xs text-slate-500">
              Categorias com maior impacto no seu orçamento anual
            </p>
          </div>

          <div className="space-y-3">
            {categoryRankings.map((cat, idx) => (
              <div key={idx} className="space-y-1.5">
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center space-x-2">
                    <span className="w-5 font-bold text-slate-400">#{idx + 1}</span>
                    <span className="font-semibold text-slate-800 dark:text-slate-200">{cat.name}</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className="font-bold text-slate-900 dark:text-white">
                      R$ {cat.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>
                    <span className="text-slate-400">({cat.percentage}%)</span>
                  </div>
                </div>
                <div className="w-full h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${cat.percentage}%`,
                      backgroundColor: cat.color || '#3B82F6'
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Financial Health Insights */}
        <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="flex items-center space-x-2 text-emerald-600">
              <Award className="w-5 h-5" />
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Saúde Financeira
              </h3>
            </div>

            <div className="p-4 rounded-xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 space-y-1">
              <span className="text-[11px] font-bold text-emerald-800 dark:text-emerald-300 uppercase">
                Status: Excelente
              </span>
              <p className="text-xs text-slate-600 dark:text-slate-300">
                Suas despesas totais representam menos de 65% das receitas geradas no ano de {selectedYear}.
              </p>
            </div>

            <div className="space-y-2 text-xs text-slate-600 dark:text-slate-300">
              <div className="flex items-center space-x-2">
                <ShieldCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                <span>Reserva de emergência acima de 70% da meta</span>
              </div>
              <div className="flex items-center space-x-2">
                <ShieldCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                <span>Gastos essenciais dentro do teto planejado</span>
              </div>
              <div className="flex items-center space-x-2">
                <ShieldCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                <span>Fluxo de caixa positivo em todos os meses</span>
              </div>
            </div>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/40 rounded-xl text-[11px] text-slate-500 border border-slate-200 dark:border-slate-700">
            Dica: Continue mantendo seus aportes automáticos para alcançar sua independência financeira antes do planejado.
          </div>
        </div>
      </div>
    </div>
  );
};
