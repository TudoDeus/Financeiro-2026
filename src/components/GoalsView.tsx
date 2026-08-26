import React, { useState } from 'react';
import { 
  PiggyBank, 
  Plus, 
  Calendar, 
  TrendingUp, 
  CheckCircle2, 
  DollarSign, 
  Clock, 
  Sparkles,
  Edit2,
  Trash2,
  Check
} from 'lucide-react';
import { FinancialGoal } from '../types';

interface GoalsViewProps {
  goals: FinancialGoal[];
  onAddGoal: (goal: Omit<FinancialGoal, 'id'>) => void;
  onUpdateGoalAmount: (goalId: string, addedAmount: number) => void;
  onDeleteGoal: (goal: FinancialGoal) => void;
}

export const GoalsView: React.FC<GoalsViewProps> = ({
  goals,
  onAddGoal,
  onUpdateGoalAmount,
  onDeleteGoal
}) => {
  const [showAddModal, setShowAddModal] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newTarget, setNewTarget] = useState('');
  const [newCurrent, setNewCurrent] = useState('');
  const [newDeadline, setNewDeadline] = useState('2026-12-31');
  const [newCategory, setNewCategory] = useState('Segurança');
  const [newNotes, setNewNotes] = useState('');

  const [activeAporteGoalId, setActiveAporteGoalId] = useState<string | null>(null);
  const [aporteInput, setAporteInput] = useState('');

  const totalSavedInGoals = goals.reduce((sum, g) => sum + g.currentAmount, 0);
  const totalTargetInGoals = goals.reduce((sum, g) => sum + g.targetAmount, 0);
  const overallProgress = totalTargetInGoals > 0 ? Math.round((totalSavedInGoals / totalTargetInGoals) * 100) : 0;

  const handleCreateGoal = (e: React.FormEvent) => {
    e.preventDefault();
    const target = parseFloat(newTarget.replace(',', '.'));
    const current = parseFloat(newCurrent.replace(',', '.') || '0');
    if (!newTitle.trim() || isNaN(target) || target <= 0) return;

    onAddGoal({
      title: newTitle.trim(),
      targetAmount: target,
      currentAmount: Math.max(0, current),
      deadline: newDeadline,
      category: newCategory,
      color: '#10B981',
      notes: newNotes.trim()
    });

    setShowAddModal(false);
    setNewTitle('');
    setNewTarget('');
    setNewCurrent('');
    setNewNotes('');
  };

  const handleAporte = (goalId: string) => {
    const val = parseFloat(aporteInput.replace(',', '.'));
    if (!isNaN(val) && val > 0) {
      onUpdateGoalAmount(goalId, val);
    }
    setActiveAporteGoalId(null);
    setAporteInput('');
  };

  return (
    <div className="space-y-6 animate-fade-in pb-12">
      {/* Top Banner */}
      <div className="p-6 rounded-2xl bg-gradient-to-r from-emerald-900 via-teal-900 to-slate-900 text-white shadow-lg space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <Sparkles className="w-5 h-5 text-emerald-400" />
              <h2 className="text-lg font-bold">Metas & Planejamento para 2026</h2>
            </div>
            <p className="text-xs text-slate-300">
              Acompanhe seu progresso de poupança, reservas e conquistas de médio e longo prazo
            </p>
          </div>

          <button
            id="open-new-goal-btn"
            onClick={() => setShowAddModal(true)}
            className="px-4 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold text-xs rounded-xl shadow-md transition flex items-center space-x-1.5 self-start sm:self-auto"
          >
            <Plus className="w-4 h-4" />
            <span>Nova Meta</span>
          </button>
        </div>

        {/* Global Progress */}
        <div className="space-y-2 pt-2 border-t border-white/10">
          <div className="flex items-center justify-between text-xs">
            <span className="text-emerald-300 font-semibold">Progresso Total ({overallProgress}%)</span>
            <span>R$ {totalSavedInGoals.toLocaleString('pt-BR')} de R$ {totalTargetInGoals.toLocaleString('pt-BR')}</span>
          </div>
          <div className="w-full h-3 bg-black/40 rounded-full overflow-hidden">
            <div
              className="h-full bg-emerald-400 rounded-full transition-all duration-500"
              style={{ width: `${Math.min(100, overallProgress)}%` }}
            />
          </div>
        </div>
      </div>

      {/* Goals Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {goals.map((g) => {
          const percent = Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100));
          const remaining = Math.max(0, g.targetAmount - g.currentAmount);
          const isFinished = g.currentAmount >= g.targetAmount;
          const isAporting = activeAporteGoalId === g.id;

          return (
            <div
              key={g.id}
              className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4 flex flex-col justify-between"
            >
              <div className="space-y-3">
                <div className="flex items-start justify-between">
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold uppercase tracking-wider bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800">
                    {g.category}
                  </span>
                  <button
                    onClick={() => onDeleteGoal(g)}
                    className="p-1 text-slate-400 hover:text-rose-600 transition rounded"
                    title="Excluir meta"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>

                <div>
                  <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                    {g.title}
                  </h3>
                  {g.notes && (
                    <p className="text-xs text-slate-500 mt-0.5">{g.notes}</p>
                  )}
                </div>

                {/* Progress bar */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs font-semibold">
                    <span className="text-slate-600 dark:text-slate-400">Progresso</span>
                    <span className={isFinished ? 'text-emerald-600 font-bold' : 'text-slate-900 dark:text-white'}>
                      {percent}%
                    </span>
                  </div>
                  <div className="w-full h-2.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${isFinished ? 'bg-emerald-500' : 'bg-emerald-600'}`}
                      style={{ width: `${percent}%` }}
                    />
                  </div>
                </div>

                {/* Amounts info */}
                <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-100 dark:border-slate-800 text-xs">
                  <div>
                    <span className="text-[11px] text-slate-500 block">Acumulado</span>
                    <span className="font-bold text-emerald-600 dark:text-emerald-400">
                      R$ {g.currentAmount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>
                  </div>
                  <div>
                    <span className="text-[11px] text-slate-500 block">Objetivo</span>
                    <span className="font-bold text-slate-900 dark:text-white">
                      R$ {g.targetAmount.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                    </span>
                  </div>
                </div>
              </div>

              {/* Aporte / Action Footer */}
              <div className="pt-3 border-t border-slate-100 dark:border-slate-800 space-y-2">
                <div className="flex items-center justify-between text-[11px] text-slate-500">
                  <span className="flex items-center space-x-1">
                    <Clock className="w-3 h-3 text-slate-400" />
                    <span>Prazo: {g.deadline.split('-').reverse().join('/')}</span>
                  </span>
                  <span>Falta: R$ {remaining.toLocaleString('pt-BR')}</span>
                </div>

                {isAporting ? (
                  <div className="flex items-center space-x-1.5 pt-1">
                    <input
                      type="number"
                      placeholder="Valor R$"
                      value={aporteInput}
                      onChange={(e) => setAporteInput(e.target.value)}
                      className="w-full px-2.5 py-1.5 text-xs rounded-lg border border-emerald-500 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                    />
                    <button
                      onClick={() => handleAporte(g.id)}
                      className="p-1.5 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700"
                      title="Salvar aporte"
                    >
                      <Check className="w-4 h-4" />
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => {
                      setActiveAporteGoalId(g.id);
                      setAporteInput('');
                    }}
                    className="w-full py-1.5 px-3 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs font-semibold text-slate-700 dark:text-slate-300 transition flex items-center justify-center space-x-1"
                  >
                    <Plus className="w-3.5 h-3.5 text-emerald-600" />
                    <span>Adicionar Aporte / Depósito</span>
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Add Goal Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 p-6 space-y-4">
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Nova Meta Financeira para 2026
            </h3>

            <form onSubmit={handleCreateGoal} className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                  Título da Meta *
                </label>
                <input
                  type="text"
                  placeholder="Ex: Reserva de Emergência, Troca de Carro..."
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  required
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                    Valor Alvo (R$) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="10000.00"
                    value={newTarget}
                    onChange={(e) => setNewTarget(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                    Valor Inicial (R$)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    value={newCurrent}
                    onChange={(e) => setNewCurrent(e.target.value)}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                    Prazo / Vencimento
                  </label>
                  <input
                    type="date"
                    value={newDeadline}
                    onChange={(e) => setNewDeadline(e.target.value)}
                    required
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                    Categoria
                  </label>
                  <select
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                  >
                    <option value="Segurança">Segurança & Reserva</option>
                    <option value="Investimentos">Investimentos & Patrimônio</option>
                    <option value="Sonhos">Sonhos & Viagens</option>
                    <option value="Aquisição">Bens & Veículos</option>
                    <option value="Educação">Educação & Carreira</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                  Notas / Detalhes
                </label>
                <input
                  type="text"
                  placeholder="Ex: Alocado em Tesouro Selic 2029"
                  value={newNotes}
                  onChange={(e) => setNewNotes(e.target.value)}
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none"
                />
              </div>

              <div className="flex items-center justify-end space-x-2 pt-3">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm"
                >
                  Criar Meta
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
