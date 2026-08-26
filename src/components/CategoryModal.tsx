import React, { useState, useEffect } from 'react';
import { 
  X, 
  Plus, 
  Tag, 
  Home, 
  Utensils, 
  Car, 
  HeartPulse, 
  GraduationCap, 
  Sparkles, 
  ShoppingBag, 
  FileText, 
  Briefcase, 
  Laptop, 
  TrendingUp, 
  PlusCircle, 
  Coffee, 
  Plane, 
  Tv, 
  Dumbbell, 
  Gift, 
  Shield, 
  Zap, 
  Droplets, 
  Wifi, 
  Smartphone, 
  PiggyBank, 
  DollarSign, 
  CreditCard, 
  Package, 
  Baby, 
  PawPrint, 
  Fuel, 
  Wrench,
  Check
} from 'lucide-react';
import { Category } from '../types';

export const CATEGORY_ICONS_MAP: Record<string, React.ComponentType<{ className?: string }>> = {
  Home,
  Utensils,
  Car,
  HeartPulse,
  GraduationCap,
  Sparkles,
  ShoppingBag,
  FileText,
  Briefcase,
  Laptop,
  TrendingUp,
  PlusCircle,
  Coffee,
  Plane,
  Tv,
  Dumbbell,
  Gift,
  Shield,
  Zap,
  Droplets,
  Wifi,
  Smartphone,
  PiggyBank,
  DollarSign,
  CreditCard,
  Package,
  Baby,
  PawPrint,
  Fuel,
  Wrench,
  Tag
};

export const POPULAR_COLORS = [
  '#10B981', // Emerald
  '#3B82F6', // Blue
  '#F59E0B', // Amber
  '#EF4444', // Red
  '#8B5CF6', // Purple
  '#EC4899', // Pink
  '#06B6D4', // Cyan
  '#64748B', // Slate
  '#84CC16', // Lime
  '#F97316', // Orange
  '#14B8A6', // Teal
  '#6366F1', // Indigo
  '#D946EF', // Fuchsia
  '#E11D48', // Rose
];

export const renderCategoryIcon = (iconName: string, className: string = 'w-4 h-4') => {
  const IconComponent = CATEGORY_ICONS_MAP[iconName] || Tag;
  return <IconComponent className={className} />;
};

interface CategoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (categoryData: Omit<Category, 'id'>, existingId?: string, oldName?: string) => void;
  initialCategory?: Category | null;
  defaultType?: 'expense' | 'income';
}

export const CategoryModal: React.FC<CategoryModalProps> = ({
  isOpen,
  onClose,
  onSave,
  initialCategory,
  defaultType = 'expense'
}) => {
  const [name, setName] = useState('');
  const [type, setType] = useState<'expense' | 'income'>(defaultType);
  const [color, setColor] = useState('#10B981');
  const [icon, setIcon] = useState('Tag');
  const [monthlyBudget, setMonthlyBudget] = useState('500');

  useEffect(() => {
    if (initialCategory) {
      setName(initialCategory.name);
      setType(initialCategory.type);
      setColor(initialCategory.color || '#10B981');
      setIcon(initialCategory.icon || 'Tag');
      setMonthlyBudget(initialCategory.monthlyBudget.toString());
    } else {
      setName('');
      setType(defaultType);
      setColor(defaultType === 'income' ? '#10B981' : '#3B82F6');
      setIcon(defaultType === 'income' ? 'TrendingUp' : 'Tag');
      setMonthlyBudget(defaultType === 'income' ? '0' : '500');
    }
  }, [initialCategory, defaultType, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    const budgetVal = parseFloat(monthlyBudget.replace(',', '.')) || 0;

    onSave(
      {
        name: name.trim(),
        type,
        color,
        icon,
        monthlyBudget: Math.max(0, budgetVal)
      },
      initialCategory?.id,
      initialCategory?.name
    );
    onClose();
  };

  const isEditing = !!initialCategory;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
      <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50/50 dark:bg-slate-800/50">
          <div className="flex items-center space-x-2">
            <div 
              className="w-8 h-8 rounded-xl flex items-center justify-center text-white shadow-sm"
              style={{ backgroundColor: color }}
            >
              {renderCategoryIcon(icon, 'w-4 h-4')}
            </div>
            <div>
              <h2 className="text-sm font-bold text-slate-900 dark:text-white">
                {isEditing ? 'Editar Categoria' : 'Nova Categoria'}
              </h2>
              <p className="text-[11px] text-slate-500">
                {isEditing ? 'Atualize as propriedades e orçamento' : 'Crie uma categoria personalizada para organizar suas finanças'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto flex-1">
          {/* Type Selector (Despesa vs Receita) */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Tipo de Categoria *
            </label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                id="cat-type-expense-btn"
                onClick={() => setType('expense')}
                className={`py-2 px-3 rounded-xl border text-xs font-bold transition flex items-center justify-center space-x-2 ${
                  type === 'expense'
                    ? 'border-rose-500 bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400'
                    : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800'
                }`}
              >
                <span>Despesa / Gasto</span>
              </button>

              <button
                type="button"
                id="cat-type-income-btn"
                onClick={() => setType('income')}
                className={`py-2 px-3 rounded-xl border text-xs font-bold transition flex items-center justify-center space-x-2 ${
                  type === 'income'
                    ? 'border-emerald-500 bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400'
                    : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800'
                }`}
              >
                <span>Receita / Entrada</span>
              </button>
            </div>
          </div>

          {/* Name */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Nome da Categoria *
            </label>
            <input
              type="text"
              required
              id="category-name-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Ex: Pets & Veterinário, Viagens, Investimentos..."
              className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none font-medium"
            />
          </div>

          {/* Monthly Budget / Expected Target */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              {type === 'expense' ? 'Teto Orçamentário Mensal (R$)' : 'Meta Mensal Prevista (R$)'}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-2.5 text-xs font-bold text-slate-400">R$</span>
              <input
                type="number"
                step="any"
                min="0"
                id="category-budget-input"
                value={monthlyBudget}
                onChange={(e) => setMonthlyBudget(e.target.value)}
                placeholder="0,00"
                className="w-full pl-9 pr-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none font-bold"
              />
            </div>
            <span className="text-[10px] text-slate-500 block mt-1">
              {type === 'expense'
                ? 'Valor máximo que você planeja gastar por mês nesta categoria no Planejador de Orçamento.'
                : 'Valor previsto a receber por mês nesta categoria.'}
            </span>
          </div>

          {/* Color Selection */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Cor de Identificação
            </label>
            <div className="flex items-center gap-2 flex-wrap mb-2">
              {POPULAR_COLORS.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setColor(c)}
                  className={`w-7 h-7 rounded-full flex items-center justify-center transition-all ${
                    color.toLowerCase() === c.toLowerCase()
                      ? 'ring-2 ring-offset-2 ring-slate-900 dark:ring-white scale-110'
                      : 'hover:scale-105 opacity-80 hover:opacity-100'
                  }`}
                  style={{ backgroundColor: c }}
                >
                  {color.toLowerCase() === c.toLowerCase() && (
                    <Check className="w-3.5 h-3.5 text-white" />
                  )}
                </button>
              ))}
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-[11px] text-slate-500">Cor personalizada:</span>
              <input
                type="color"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                className="w-7 h-7 rounded-lg cursor-pointer border border-slate-200 dark:border-slate-700 p-0.5 bg-transparent"
              />
              <span className="text-xs font-mono text-slate-600 dark:text-slate-400 uppercase font-bold">{color}</span>
            </div>
          </div>

          {/* Icon Selection */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Ícone da Categoria
            </label>
            <div className="grid grid-cols-6 sm:grid-cols-8 gap-2 p-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/30 max-h-40 overflow-y-auto">
              {Object.keys(CATEGORY_ICONS_MAP).map((iconKey) => {
                const isSelected = icon === iconKey;
                return (
                  <button
                    key={iconKey}
                    type="button"
                    onClick={() => setIcon(iconKey)}
                    className={`p-2 rounded-xl flex items-center justify-center transition ${
                      isSelected
                        ? 'bg-emerald-600 text-white shadow-sm ring-2 ring-emerald-500'
                        : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700'
                    }`}
                    title={iconKey}
                  >
                    {renderCategoryIcon(iconKey, 'w-4 h-4')}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Action Buttons */}
          <div className="pt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-end space-x-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition"
            >
              Cancelar
            </button>
            <button
              type="submit"
              id="category-submit-save-btn"
              className="px-5 py-2 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white transition shadow-sm shadow-emerald-600/20 flex items-center space-x-1.5"
            >
              {isEditing ? (
                <span>Salvar Alterações</span>
              ) : (
                <>
                  <Plus className="w-4 h-4" />
                  <span>Cadastrar Categoria</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
