import React, { useState } from 'react';
import { 
  Plus, 
  Search, 
  Filter, 
  Download, 
  FileSpreadsheet, 
  ArrowUpRight, 
  ArrowDownRight, 
  ArrowLeftRight, 
  Edit2, 
  Trash2, 
  CheckCircle2, 
  Clock,
  RotateCcw,
  Layers,
  Repeat
} from 'lucide-react';
import { Account, Category, CreditCard, Transaction } from '../types';
import { MONTH_NAMES } from '../data/initialData';

interface TransactionsViewProps {
  transactions: Transaction[];
  categories: Category[];
  accounts: Account[];
  creditCards: CreditCard[];
  selectedMonth: number;
  selectedYear: number;
  onOpenNewTransaction: () => void;
  onEditTransaction: (t: Transaction) => void;
  onRequestDeleteTransaction: (t: Transaction) => void;
  onToggleStatus: (id: string) => void;
  onResetMonth: () => void;
}

export const TransactionsView: React.FC<TransactionsViewProps> = ({
  transactions,
  categories,
  accounts,
  creditCards,
  selectedMonth,
  selectedYear,
  onOpenNewTransaction,
  onEditTransaction,
  onRequestDeleteTransaction,
  onToggleStatus,
  onResetMonth
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('all');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');
  const [accountFilter, setAccountFilter] = useState<string>('all');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [formatFilter, setFormatFilter] = useState<string>('all'); // all, installment, recurring, single
  const [onlyCurrentMonth, setOnlyCurrentMonth] = useState<boolean>(true);

  const currentMonthPrefix = `${selectedYear}-${String(selectedMonth + 1).padStart(2, '0')}`;

  const filteredTransactions = transactions.filter((t) => {
    if (onlyCurrentMonth && !t.date.startsWith(currentMonthPrefix)) {
      return false;
    }
    if (searchTerm) {
      const matchDesc = t.description.toLowerCase().includes(searchTerm.toLowerCase());
      const matchCat = t.category.toLowerCase().includes(searchTerm.toLowerCase());
      const matchAcc = t.account.toLowerCase().includes(searchTerm.toLowerCase());
      if (!matchDesc && !matchCat && !matchAcc) return false;
    }
    if (typeFilter !== 'all' && t.type !== typeFilter) return false;
    if (categoryFilter !== 'all' && t.category !== categoryFilter) return false;
    if (accountFilter !== 'all' && t.account !== accountFilter) return false;
    if (statusFilter !== 'all' && t.status !== statusFilter) return false;
    if (formatFilter === 'installment' && !t.isInstallment && !t.installmentGroupId) return false;
    if (formatFilter === 'recurring' && !t.isRecurring && !t.recurrenceGroupId) return false;
    if (formatFilter === 'single' && (t.isInstallment || t.installmentGroupId || t.isRecurring || t.recurrenceGroupId)) return false;
    return true;
  });

  const totalFilteredIncome = filteredTransactions
    .filter(t => t.type === 'income')
    .reduce((sum, t) => sum + t.amount, 0);

  const totalFilteredExpense = filteredTransactions
    .filter(t => t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0);

  const handleExportCSV = () => {
    const headers = ['Data', 'Descrição', 'Valor', 'Tipo', 'Categoria', 'Conta/Cartão', 'Status', 'Formato', 'Observações'];
    const rows = filteredTransactions.map(t => [
      t.date,
      `"${t.description.replace(/"/g, '""')}"`,
      t.amount.toFixed(2),
      t.type,
      `"${t.category}"`,
      `"${t.account}"`,
      t.status,
      t.isInstallment ? `Parcelado (${t.installmentCurrent}/${t.installmentTotal})` : t.isRecurring ? 'Fixo Recorrente' : 'À Vista',
      `"${(t.notes || '').replace(/"/g, '""')}"`
    ]);

    const csvContent = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `financas_${selectedYear}_${selectedMonth + 1}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-5 animate-fade-in pb-12">
      {/* Top action & search bar */}
      <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
        <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
          {/* Search box */}
          <div className="relative w-full sm:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              id="search-transactions-input"
              type="text"
              placeholder="Buscar por nome, categoria, conta..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
          </div>

          {/* Action buttons */}
          <div className="flex items-center space-x-2 w-full sm:w-auto justify-end flex-wrap gap-y-2">
            <button
              id="reset-month-transactions-btn"
              onClick={onResetMonth}
              title={`Limpar todos os lançamentos de ${MONTH_NAMES[selectedMonth]} ${selectedYear}`}
              className="px-3 py-2 border border-rose-200 dark:border-rose-900/60 rounded-xl text-xs font-semibold text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition flex items-center space-x-1.5"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Resetar Mês</span>
            </button>

            <button
              id="export-csv-btn"
              onClick={handleExportCSV}
              className="px-3 py-2 border border-slate-200 dark:border-slate-700 rounded-xl text-xs font-semibold text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition flex items-center space-x-1.5"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Exportar CSV</span>
            </button>

            <button
              id="transactions-new-btn"
              onClick={onOpenNewTransaction}
              className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-sm transition flex items-center space-x-1.5"
            >
              <Plus className="w-4 h-4" />
              <span>Novo Lançamento</span>
            </button>
          </div>
        </div>

        {/* Filter Controls Row */}
        <div className="flex flex-wrap gap-2 pt-2 border-t border-slate-100 dark:border-slate-800 text-xs">
          {/* Format Filter */}
          <select
            id="filter-format-select"
            value={formatFilter}
            onChange={(e) => setFormatFilter(e.target.value)}
            className="px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 focus:outline-none font-medium"
          >
            <option value="all">Todos Formatos</option>
            <option value="installment">📑 Parcelados (Cartão, Boleto, Carnê, Conta)</option>
            <option value="recurring">🔄 Fixos / Recorrentes</option>
            <option value="single">⚡ À Vista / Pontuais</option>
          </select>

          {/* Type Filter */}
          <select
            id="filter-type-select"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 focus:outline-none"
          >
            <option value="all">Todos os Tipos</option>
            <option value="income">Receitas (+)</option>
            <option value="expense">Despesas (-)</option>
            <option value="transfer">Transferências</option>
          </select>

          {/* Category Filter */}
          <select
            id="filter-category-select"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 focus:outline-none"
          >
            <option value="all">Todas Categorias</option>
            {categories.map((c) => (
              <option key={c.id} value={c.name}>
                {c.name}
              </option>
            ))}
          </select>

          {/* Account Filter */}
          <select
            id="filter-account-select"
            value={accountFilter}
            onChange={(e) => setAccountFilter(e.target.value)}
            className="px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 focus:outline-none"
          >
            <option value="all">Todas as Contas / Cartões</option>
            {creditCards.map((c) => (
              <option key={c.id} value={c.name}>
                💳 {c.name}
              </option>
            ))}
            {accounts.map((a) => (
              <option key={a.id} value={a.name}>
                🏛️ {a.name}
              </option>
            ))}
          </select>

          {/* Status Filter */}
          <select
            id="filter-status-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 focus:outline-none"
          >
            <option value="all">Todos os Status</option>
            <option value="completed">Concluídos / Pagos</option>
            <option value="pending">Pendentes / Agendados</option>
          </select>

          {/* Month switch */}
          <button
            onClick={() => setOnlyCurrentMonth(!onlyCurrentMonth)}
            className={`px-2.5 py-1.5 rounded-lg border text-xs font-semibold transition ${
              onlyCurrentMonth
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800'
                : 'bg-slate-100 text-slate-600 border-slate-200 dark:bg-slate-800 dark:text-slate-400 dark:border-slate-700'
            }`}
          >
            {onlyCurrentMonth ? 'Apenas Mês Selecionado' : 'Todos os Meses de 2026'}
          </button>
        </div>
      </div>

      {/* Summary for current filter */}
      <div className="flex items-center justify-between text-xs px-2 text-slate-600 dark:text-slate-400">
        <span>Exibindo {filteredTransactions.length} lançamentos</span>
        <div className="flex items-center space-x-4 font-semibold">
          <span className="text-emerald-600">Entradas: R$ {totalFilteredIncome.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</span>
          <span className="text-rose-600">Saídas: R$ {totalFilteredExpense.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</span>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800/60 border-b border-slate-200 dark:border-slate-800 text-slate-500 dark:text-slate-400 uppercase tracking-wider text-[10px] font-semibold">
              <tr>
                <th className="py-3 px-4">Data</th>
                <th className="py-3 px-4">Descrição & Formato</th>
                <th className="py-3 px-4">Categoria</th>
                <th className="py-3 px-4">Conta / Cartão</th>
                <th className="py-3 px-4 text-right">Valor</th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-center">Planilha</th>
                <th className="py-3 px-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {filteredTransactions.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-10 text-center text-slate-400">
                    Nenhum lançamento encontrado para os filtros selecionados.
                  </td>
                </tr>
              ) : (
                filteredTransactions.map((t) => (
                  <tr 
                    key={t.id}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition group"
                  >
                    {/* Date */}
                    <td className="py-3.5 px-4 font-mono text-slate-600 dark:text-slate-400 whitespace-nowrap">
                      {t.date.split('-').reverse().join('/')}
                    </td>

                    {/* Description & Badges */}
                    <td className="py-3.5 px-4 font-bold text-slate-900 dark:text-white">
                      <div className="flex items-center space-x-2">
                        <div className={`p-1 rounded-md flex-shrink-0 ${
                          t.type === 'income'
                            ? 'text-emerald-600 bg-emerald-50 dark:bg-emerald-950/50'
                            : t.type === 'transfer'
                            ? 'text-blue-600 bg-blue-50 dark:bg-blue-950/50'
                            : 'text-rose-600 bg-rose-50 dark:bg-rose-950/50'
                        }`}>
                          {t.type === 'income' ? <ArrowUpRight className="w-3.5 h-3.5" /> : t.type === 'transfer' ? <ArrowLeftRight className="w-3.5 h-3.5" /> : <ArrowDownRight className="w-3.5 h-3.5" />}
                        </div>
                        <div className="space-y-0.5">
                          <div className="flex items-center space-x-1.5 flex-wrap gap-y-1">
                            <span className="truncate block max-w-xs">{t.description}</span>
                            
                            {/* Installment Badge */}
                            {(t.isInstallment || t.installmentCurrent) && (
                              <span className="inline-flex items-center space-x-1 px-1.5 py-0.5 rounded-md bg-indigo-50 dark:bg-indigo-950/70 border border-indigo-200 dark:border-indigo-800 text-indigo-700 dark:text-indigo-300 text-[10px] font-bold">
                                <Layers className="w-2.5 h-2.5" />
                                <span>{t.installmentCurrent}/{t.installmentTotal || '?'}</span>
                              </span>
                            )}

                            {/* Recurring Badge */}
                            {(t.isRecurring || t.recurrenceGroupId) && (
                              <span className="inline-flex items-center space-x-1 px-1.5 py-0.5 rounded-md bg-teal-50 dark:bg-teal-950/70 border border-teal-200 dark:border-teal-800 text-teal-700 dark:text-teal-300 text-[10px] font-bold">
                                <Repeat className="w-2.5 h-2.5" />
                                <span>Fixo</span>
                              </span>
                            )}
                          </div>
                          {t.notes && <span className="text-[10px] text-slate-400 font-normal block">{t.notes}</span>}
                        </div>
                      </div>
                    </td>

                    {/* Category */}
                    <td className="py-3.5 px-4 text-slate-700 dark:text-slate-300">
                      {(() => {
                        const catObj = categories.find(c => c.name === t.category);
                        return (
                          <span className="inline-flex items-center space-x-1.5 px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 text-[11px] font-medium border border-slate-200 dark:border-slate-700">
                            {catObj && (
                              <span
                                className="w-2 h-2 rounded-full flex-shrink-0"
                                style={{ backgroundColor: catObj.color }}
                              />
                            )}
                            <span className="truncate max-w-[140px]">{t.category}</span>
                          </span>
                        );
                      })()}
                    </td>

                    {/* Account */}
                    <td className="py-3.5 px-4 text-slate-600 dark:text-slate-400 whitespace-nowrap">
                      <div className="flex items-center space-x-1">
                        {creditCards.some(c => c.name === t.account) ? (
                          <span className="text-indigo-500 font-medium">💳 {t.account}</span>
                        ) : (
                          <span>🏛️ {t.account}</span>
                        )}
                      </div>
                    </td>

                    {/* Amount */}
                    <td className="py-3.5 px-4 text-right font-black whitespace-nowrap">
                      <span className={t.type === 'income' ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-900 dark:text-white'}>
                        {t.type === 'income' ? '+' : '-'} R$ {t.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </span>
                    </td>

                    {/* Status */}
                    <td className="py-3.5 px-4 text-center whitespace-nowrap">
                      <button
                        onClick={() => onToggleStatus(t.id)}
                        className={`inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[11px] font-semibold transition ${
                          t.status === 'completed'
                            ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800'
                            : 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300 border border-amber-200 dark:border-amber-800'
                        }`}
                        title="Clique para alternar status"
                      >
                        {t.status === 'completed' ? (
                          <>
                            <CheckCircle2 className="w-3 h-3" />
                            <span>Pago</span>
                          </>
                        ) : (
                          <>
                            <Clock className="w-3 h-3" />
                            <span>Pendente</span>
                          </>
                        )}
                      </button>
                    </td>

                    {/* Sync Status */}
                    <td className="py-3.5 px-4 text-center whitespace-nowrap">
                      {t.syncedWithSheet ? (
                        <span className="inline-flex items-center text-emerald-600 dark:text-emerald-400 text-[11px]" title="Sincronizado na planilha">
                          <FileSpreadsheet className="w-3.5 h-3.5 mr-1" />
                          <span>Sim</span>
                        </span>
                      ) : (
                        <span className="text-slate-400 text-[11px]">Local</span>
                      )}
                    </td>

                    {/* Actions */}
                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <div className="flex items-center justify-end space-x-1">
                        <button
                          id={`edit-transaction-${t.id}`}
                          onClick={() => onEditTransaction(t)}
                          className="p-1.5 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition"
                          title="Editar lançamento"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                        </button>
                        <button
                          id={`delete-transaction-${t.id}`}
                          onClick={() => onRequestDeleteTransaction(t)}
                          className="p-1.5 text-rose-400 hover:text-rose-600 dark:hover:text-rose-300 hover:bg-rose-50 dark:hover:bg-rose-950/40 rounded-lg transition"
                          title={t.isInstallment ? "Excluir parcela ou compra" : t.isRecurring ? "Excluir lançamento fixo" : "Excluir lançamento"}
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
