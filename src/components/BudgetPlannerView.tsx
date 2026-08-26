import React, { useState } from 'react';
import { 
  Target, 
  TrendingUp, 
  TrendingDown,
  AlertTriangle, 
  CheckCircle, 
  Sliders, 
  Edit3, 
  Save, 
  X,
  Plus,
  Trash2,
  Settings2,
  PieChart as PieIcon,
  Tag
} from 'lucide-react';
import { Category, Transaction } from '../types';
import { MONTH_NAMES } from '../data/initialData';
import { renderCategoryIcon } from './CategoryModal';

interface BudgetPlannerViewProps {
  selectedMonth: number;
  selectedYear: number;
  categories: Category[];
  transactions: Transaction[];
  onUpdateCategoryBudget: (categoryId: string, newBudget: number) => void;
  onOpenAddCategory: (defaultType?: 'expense' | 'income') => void;
  onOpenEditCategory: (category: Category) => void;
  onDeleteCategory: (category: Category) => void;
}

export const BudgetPlannerView: React.FC<BudgetPlannerViewProps> = ({
  selectedMonth,
  selectedYear,
  categories,
  transactions,
  onUpdateCategoryBudget,
  onOpenAddCategory,
  onOpenEditCategory,
  onDeleteCategory
}) => {
  const [activeTab, setActiveTab] = useState<'expense' | 'income'>('expense');
  const [editingCatId, setEditingCatId] = useState<string | null>(null);
  const [editBudgetValue, setEditBudgetValue] = useState<string>('');

  const currentMonthPrefix = `${selectedYear}-${String(selectedMonth + 1).padStart(2, '0')}`;
  const monthTransactions = transactions.filter(t => t.date.startsWith(currentMonthPrefix));

  const expenseCategories = categories.filter(c => c.type === 'expense');
  const incomeCategories = categories.filter(c => c.type === 'income');
  const displayedCategories = activeTab === 'expense' ? expenseCategories : incomeCategories;

  // Compute Expense stats
  const totalPlannedExpense = expenseCategories.reduce((sum, c) => sum + c.monthlyBudget, 0);
  const totalActualExpense = monthTransactions
    .filter(t => t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0);
  const totalRemainingExpense = totalPlannedExpense - totalActualExpense;
  const overallExpensePercentage = totalPlannedExpense > 0 ? Math.round((totalActualExpense / totalPlannedExpense) * 100) : 0;

  // Compute Income stats
  const totalPlannedIncome = incomeCategories.reduce((sum, c) => sum + c.monthlyBudget, 0);
  const totalActualIncome = monthTransactions
    .filter(t => t.type === 'income')
    .reduce((sum, t) => sum + t.amount, 0);
  const overallIncomePercentage = totalPlannedIncome > 0 ? Math.round((totalActualIncome / totalPlannedIncome) * 100) : 0;

  const handleStartEdit = (cat: Category) => {
    setEditingCatId(cat.id);
    setEditBudgetValue(cat.monthlyBudget.toString());
  };

  const handleSaveEdit = (catId: string) => {
    const val = parseFloat(editBudgetValue.replace(',', '.'));
    if (!isNaN(val) && val >= 0) {
      onUpdateCategoryBudget(catId, val);
    }
    setEditingCatId(null);
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Header Overview Card */}
      <div className="p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center space-x-2">
              <Target className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
              <h2 className="text-base font-bold text-slate-900 dark:text-white">
                Planejamento & Gestão de Categorias ({MONTH_NAMES[selectedMonth]} {selectedYear})
              </h2>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Crie novas categorias, ajuste tetos de gastos e monitore suas metas mensais
            </p>
          </div>

          <div className="flex items-center space-x-2">
            <button
              id="add-category-btn-budget-view"
              onClick={() => onOpenAddCategory(activeTab)}
              className="flex items-center space-x-1.5 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold transition shadow-sm shadow-emerald-600/20"
            >
              <Plus className="w-4 h-4" />
              <span>Nova Categoria</span>
            </button>
          </div>
        </div>

        {/* Global Progress Bar (for Expenses) */}
        {activeTab === 'expense' ? (
          <div className="space-y-1.5 pt-2">
            <div className="flex items-center justify-between text-xs">
              <span className="font-semibold text-slate-700 dark:text-slate-300">Consumo Geral do Orçamento de Despesas</span>
              <span className="font-bold text-slate-900 dark:text-white">{overallExpensePercentage}% do teto utilizado</span>
            </div>
            <div className="w-full h-3 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${
                  overallExpensePercentage > 100
                    ? 'bg-rose-500'
                    : overallExpensePercentage > 85
                    ? 'bg-amber-500'
                    : 'bg-emerald-500'
                }`}
                style={{ width: `${Math.min(100, overallExpensePercentage)}%` }}
              />
            </div>
            <div className="flex justify-between text-xs text-slate-500">
              <span>Gasto Real: R$ {totalActualExpense.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</span>
              <span>Teto Orçado: R$ {totalPlannedExpense.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</span>
            </div>
          </div>
        ) : (
          <div className="space-y-1.5 pt-2">
            <div className="flex items-center justify-between text-xs">
              <span className="font-semibold text-slate-700 dark:text-slate-300">Alcance da Meta de Receitas Prevista</span>
              <span className="font-bold text-emerald-600 dark:text-emerald-400">{overallIncomePercentage}% da meta atingida</span>
            </div>
            <div className="w-full h-3 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full rounded-full bg-emerald-500 transition-all duration-500"
                style={{ width: `${Math.min(100, overallIncomePercentage)}%` }}
              />
            </div>
            <div className="flex justify-between text-xs text-slate-500">
              <span>Recebido Real: R$ {totalActualIncome.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</span>
              <span>Meta Prevista: R$ {totalPlannedIncome.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</span>
            </div>
          </div>
        )}

        {/* 3 Metric counters */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
          <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
            <span className="text-[11px] font-semibold text-slate-500 uppercase">
              {activeTab === 'expense' ? 'Teto Orçado Total' : 'Previsão de Receita'}
            </span>
            <p className="text-lg font-black text-slate-900 dark:text-white">
              R$ {(activeTab === 'expense' ? totalPlannedExpense : totalPlannedIncome).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
            <span className="text-[11px] font-semibold text-slate-500 uppercase">
              {activeTab === 'expense' ? 'Realizado (Despesas)' : 'Realizado (Receitas)'}
            </span>
            <p className={`text-lg font-black ${activeTab === 'expense' ? 'text-rose-600 dark:text-rose-400' : 'text-emerald-600 dark:text-emerald-400'}`}>
              R$ {(activeTab === 'expense' ? totalActualExpense : totalActualIncome).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
            <span className="text-[11px] font-semibold text-slate-500 uppercase">
              {activeTab === 'expense' ? 'Disponível para Gastar' : 'Diferença da Meta'}
            </span>
            <p className={`text-lg font-black ${
              activeTab === 'expense' 
                ? (totalRemainingExpense >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400')
                : (totalActualIncome >= totalPlannedIncome ? 'text-emerald-600 dark:text-emerald-400' : 'text-amber-500')
            }`}>
              R$ {(activeTab === 'expense' ? totalRemainingExpense : (totalActualIncome - totalPlannedIncome)).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
            </p>
          </div>
        </div>
      </div>

      {/* Tabs: Expense Categories vs Income Categories */}
      <div className="flex items-center justify-between gap-3 border-b border-slate-200 dark:border-slate-800 pb-3">
        <div className="flex items-center space-x-2">
          <button
            type="button"
            id="tab-expense-categories"
            onClick={() => setActiveTab('expense')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center space-x-2 ${
              activeTab === 'expense'
                ? 'bg-rose-500 text-white shadow-sm shadow-rose-500/20'
                : 'bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-400 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800'
            }`}
          >
            <TrendingDown className="w-4 h-4" />
            <span>Categorias de Despesas ({expenseCategories.length})</span>
          </button>

          <button
            type="button"
            id="tab-income-categories"
            onClick={() => setActiveTab('income')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center space-x-2 ${
              activeTab === 'income'
                ? 'bg-emerald-600 text-white shadow-sm shadow-emerald-600/20'
                : 'bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-400 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800'
            }`}
          >
            <TrendingUp className="w-4 h-4" />
            <span>Categorias de Receitas ({incomeCategories.length})</span>
          </button>
        </div>

        <button
          type="button"
          onClick={() => onOpenAddCategory(activeTab)}
          className="text-xs font-bold text-emerald-600 dark:text-emerald-400 hover:underline flex items-center space-x-1"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Nova {activeTab === 'expense' ? 'Despesa' : 'Receita'}</span>
        </button>
      </div>

      {/* Categories Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {displayedCategories.map((cat) => {
          const isExpense = cat.type === 'expense';
          const totalAmount = monthTransactions
            .filter(t => t.type === cat.type && t.category === cat.name)
            .reduce((sum, t) => sum + t.amount, 0);

          const percent = cat.monthlyBudget > 0 ? Math.round((totalAmount / cat.monthlyBudget) * 100) : 0;
          const remaining = cat.monthlyBudget - totalAmount;
          const isOverBudget = isExpense && remaining < 0;
          const isEditing = editingCatId === cat.id;

          return (
            <div
              key={cat.id}
              className={`p-5 rounded-2xl bg-white dark:bg-slate-900 border transition-all shadow-sm space-y-3 relative group ${
                isOverBudget 
                  ? 'border-rose-300 dark:border-rose-900/60 bg-rose-50/10' 
                  : 'border-slate-200 dark:border-slate-800'
              }`}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center space-x-3 min-w-0">
                  <div
                    className="w-10 h-10 rounded-xl flex items-center justify-center text-white flex-shrink-0 shadow-sm"
                    style={{ backgroundColor: cat.color || (isExpense ? '#EF4444' : '#10B981') }}
                  >
                    {renderCategoryIcon(cat.icon, 'w-5 h-5')}
                  </div>
                  <div className="min-w-0">
                    <div className="flex items-center space-x-2">
                      <h3 className="text-xs font-bold text-slate-900 dark:text-white truncate">
                        {cat.name}
                      </h3>
                      <span className={`text-[10px] px-2 py-0.5 rounded-full font-semibold border ${
                        isExpense 
                          ? 'bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400 border-rose-200 dark:border-rose-800/60'
                          : 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800/60'
                      }`}>
                        {isExpense ? 'Despesa' : 'Receita'}
                      </span>
                    </div>
                    <span className="text-[11px] text-slate-500 block truncate">
                      {totalAmount > 0 
                        ? `${isExpense ? 'Gasto:' : 'Recebido:'} R$ ${totalAmount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })} no mês` 
                        : 'Sem movimentação neste mês'}
                    </span>
                  </div>
                </div>

                {/* Edit / Delete Action Buttons */}
                <div className="flex items-center space-x-1">
                  <button
                    onClick={() => onOpenEditCategory(cat)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 transition"
                    title="Editar nome, cor, ícone e teto da categoria"
                  >
                    <Settings2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => onDeleteCategory(cat)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition"
                    title="Excluir categoria"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {/* Quick Inline Budget Edit or View */}
              <div className="flex items-center justify-between text-xs pt-1 border-t border-slate-100 dark:border-slate-800/60">
                <span className="text-[11px] text-slate-500 font-medium">
                  {isExpense ? 'Teto Orçamentário:' : 'Meta Prevista:'}
                </span>

                {isEditing ? (
                  <div className="flex items-center space-x-1">
                    <span className="text-[10px] text-slate-400">R$</span>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      value={editBudgetValue}
                      onChange={(e) => setEditBudgetValue(e.target.value)}
                      className="w-24 px-2 py-0.5 text-xs font-bold border rounded-lg border-emerald-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                    />
                    <button
                      onClick={() => handleSaveEdit(cat.id)}
                      className="p-1 rounded-md bg-emerald-600 text-white hover:bg-emerald-700"
                    >
                      <Save className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => setEditingCatId(null)}
                      className="p-1 rounded-md text-slate-400 hover:text-slate-600"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => handleStartEdit(cat)}
                    className="flex items-center space-x-1 text-[11px] text-slate-700 dark:text-slate-300 hover:text-emerald-600 dark:hover:text-emerald-400 font-bold px-2 py-0.5 rounded hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                    title="Clique para editar rapidamente o valor"
                  >
                    <span>R$ {cat.monthlyBudget.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</span>
                    <Edit3 className="w-3 h-3 text-slate-400 ml-1" />
                  </button>
                )}
              </div>

              {/* Progress meter */}
              {cat.monthlyBudget > 0 && (
                <div className="space-y-1">
                  <div className="w-full h-2.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-300 ${
                        isExpense
                          ? percent > 100
                            ? 'bg-rose-500'
                            : percent > 80
                            ? 'bg-amber-500'
                            : 'bg-emerald-500'
                          : 'bg-emerald-500'
                      }`}
                      style={{ width: `${Math.min(100, percent)}%` }}
                    />
                  </div>
                  <div className="flex items-center justify-between text-[11px]">
                    <span className="font-semibold text-slate-700 dark:text-slate-300">
                      {percent}% {isExpense ? 'consumido' : 'atingido'}
                    </span>
                    <span className={isOverBudget ? 'text-rose-600 font-bold' : 'text-slate-500'}>
                      {isExpense ? (
                        isOverBudget 
                          ? `Excedido em R$ ${Math.abs(remaining).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}` 
                          : `R$ ${remaining.toLocaleString('pt-BR', { minimumFractionDigits: 2 })} restantes`
                      ) : (
                        remaining <= 0 
                          ? `Superada em R$ ${Math.abs(remaining).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`
                          : `Faltam R$ ${remaining.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`
                      )}
                    </span>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
