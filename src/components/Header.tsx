import React from 'react';
import { 
  Wallet, 
  Calendar, 
  RefreshCw, 
  Plus, 
  FileSpreadsheet, 
  CheckCircle2, 
  AlertCircle, 
  LogOut, 
  User as UserIcon,
  RotateCcw,
  ChevronDown,
  Smartphone
} from 'lucide-react';
import { User } from 'firebase/auth';
import { MONTH_NAMES } from '../data/initialData';
import { GoogleSheetMeta } from '../types';

interface HeaderProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  selectedMonth: number; // 0 - 11
  setSelectedMonth: (month: number) => void;
  selectedYear: number;
  user: User | null;
  sheetMeta: GoogleSheetMeta | null;
  isSyncing: boolean;
  onOpenSyncModal: () => void;
  onOpenNewTransaction: () => void;
  onResetMonth: () => void;
  onOpenInstallModal: () => void;
  onLogin: () => void;
  onLogout: () => void;
  isLoggingIn: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  currentTab,
  setCurrentTab,
  selectedMonth,
  setSelectedMonth,
  selectedYear,
  user,
  sheetMeta,
  isSyncing,
  onOpenSyncModal,
  onOpenNewTransaction,
  onResetMonth,
  onOpenInstallModal,
  onLogin,
  onLogout,
  isLoggingIn
}) => {
  const [showUserMenu, setShowUserMenu] = React.useState(false);

  const tabs = [
    { id: 'dashboard', label: 'Visão Geral' },
    { id: 'transactions', label: 'Lançamentos' },
    { id: 'budget', label: 'Orçamento' },
    { id: 'accounts', label: 'Contas & Cartões' },
    { id: 'goals', label: 'Metas 2026' },
    { id: 'reports', label: 'Relatórios' }
  ];

  return (
    <header className="sticky top-0 z-40 bg-white/95 dark:bg-slate-900/95 backdrop-blur border-b border-slate-200 dark:border-slate-800 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Top bar */}
        <div className="flex items-center justify-between h-16 gap-3">
          {/* Logo & Title */}
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-600 flex items-center justify-center text-white shadow-md shadow-emerald-600/20">
              <Wallet className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight">
                  Controle Financeiro
                </h1>
                <span className="px-2 py-0.5 text-xs font-semibold bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 rounded-full border border-emerald-200 dark:border-emerald-800">
                  {selectedYear}
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 hidden sm:block">
                Gestão Inteligente & Planilha Google Sheets
              </p>
            </div>
          </div>

          {/* Month Navigator & Actions */}
          <div className="flex items-center space-x-2 sm:space-x-3">
            {/* Month selector dropdown & Reset button */}
            <div className="flex items-center space-x-1">
              <div className="relative flex items-center bg-slate-100 dark:bg-slate-800 rounded-xl p-1 border border-slate-200 dark:border-slate-700">
                <Calendar className="w-4 h-4 text-slate-500 dark:text-slate-400 ml-2 hidden sm:inline" />
                <select
                  id="month-select-dropdown"
                  value={selectedMonth}
                  onChange={(e) => setSelectedMonth(Number(e.target.value))}
                  className="bg-transparent text-sm font-medium text-slate-800 dark:text-slate-200 py-1 px-2.5 focus:outline-none cursor-pointer"
                >
                  {MONTH_NAMES.map((name, idx) => (
                    <option key={idx} value={idx} className="dark:bg-slate-900 dark:text-slate-200">
                      {name} {selectedYear}
                    </option>
                  ))}
                </select>
              </div>

              <button
                id="header-reset-month-btn"
                onClick={onResetMonth}
                title={`Resetar dados de ${MONTH_NAMES[selectedMonth]} ${selectedYear}`}
                className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40 dark:hover:text-rose-400 transition flex items-center space-x-1"
              >
                <RotateCcw className="w-4 h-4" />
                <span className="hidden xl:inline text-xs font-semibold">Resetar Mês</span>
              </button>
            </div>

            {/* Install / APK button */}
            <button
              id="install-mobile-app-btn"
              onClick={onOpenInstallModal}
              className="flex items-center space-x-1.5 px-2.5 py-2 rounded-xl text-xs font-semibold border border-indigo-200 dark:border-indigo-800/80 bg-indigo-50/70 dark:bg-indigo-950/40 text-indigo-700 dark:text-indigo-300 hover:bg-indigo-100 transition shadow-sm"
              title="Instalar App no Celular / APK"
            >
              <Smartphone className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
              <span className="hidden lg:inline">App Celular / APK</span>
            </button>

            {/* Google Sheets Sync Pill */}
            <button
              id="google-sheets-sync-btn"
              onClick={onOpenSyncModal}
              className={`flex items-center space-x-2 px-3 py-2 rounded-xl text-xs font-medium border transition-all ${
                sheetMeta
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800/80 hover:bg-emerald-100'
                  : 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700 hover:bg-slate-200'
              }`}
              title="Gerenciar conexão com Planilha Google"
            >
              <FileSpreadsheet className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
              <span className="hidden md:inline font-semibold">
                {sheetMeta ? sheetMeta.title.substring(0, 16) + '...' : 'Conectar Planilha'}
              </span>
              {isSyncing ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin text-emerald-600" />
              ) : sheetMeta ? (
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              ) : (
                <AlertCircle className="w-3.5 h-3.5 text-amber-500" />
              )}
            </button>

            {/* User Auth / Sign in with Google */}
            {user ? (
              <div className="relative">
                <button
                  id="user-profile-menu-btn"
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center space-x-2 p-1.5 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition"
                >
                  {user.photoURL ? (
                    <img
                      src={user.photoURL}
                      alt={user.displayName || 'Usuário'}
                      referrerPolicy="no-referrer"
                      className="w-7 h-7 rounded-lg object-cover"
                    />
                  ) : (
                    <div className="w-7 h-7 rounded-lg bg-emerald-600 text-white flex items-center justify-center text-xs font-bold">
                      {(user.displayName || user.email || 'U')[0].toUpperCase()}
                    </div>
                  )}
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                </button>

                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-56 bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 py-2 z-50 animate-fade-in">
                    <div className="px-4 py-2 border-b border-slate-100 dark:border-slate-800">
                      <p className="text-xs font-semibold text-slate-900 dark:text-white truncate">
                        {user.displayName || 'Usuário Conectado'}
                      </p>
                      <p className="text-xs text-slate-500 truncate">{user.email}</p>
                    </div>
                    <button
                      id="header-open-sync-btn"
                      onClick={() => {
                        setShowUserMenu(false);
                        onOpenSyncModal();
                      }}
                      className="w-full px-4 py-2 text-left text-xs text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 flex items-center space-x-2"
                    >
                      <FileSpreadsheet className="w-4 h-4 text-emerald-600" />
                      <span>Configurações da Planilha</span>
                    </button>
                    <button
                      id="header-logout-btn"
                      onClick={() => {
                        setShowUserMenu(false);
                        onLogout();
                      }}
                      className="w-full px-4 py-2 text-left text-xs text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/30 flex items-center space-x-2"
                    >
                      <LogOut className="w-4 h-4" />
                      <span>Desconectar Conta Google</span>
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <button
                id="header-google-signin-btn"
                onClick={onLogin}
                disabled={isLoggingIn}
                className="flex items-center space-x-2 px-3 py-2 bg-slate-900 dark:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-semibold hover:bg-slate-800 dark:hover:bg-slate-100 transition shadow-sm"
              >
                <UserIcon className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Entrar com Google</span>
                <span className="sm:hidden">Entrar</span>
              </button>
            )}

            {/* Quick Add Transaction Button */}
            <button
              id="header-new-transaction-btn"
              onClick={onOpenNewTransaction}
              className="flex items-center space-x-1.5 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold transition shadow-sm shadow-emerald-600/20"
            >
              <Plus className="w-4 h-4" />
              <span className="hidden sm:inline">Novo Lançamento</span>
            </button>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="flex space-x-1 overflow-x-auto no-scrollbar py-2 border-t border-slate-100 dark:border-slate-800/60">
          {tabs.map((tab) => {
            const isActive = currentTab === tab.id;
            return (
              <button
                key={tab.id}
                id={`nav-tab-${tab.id}`}
                onClick={() => setCurrentTab(tab.id)}
                className={`px-3.5 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-all ${
                  isActive
                    ? 'bg-emerald-600 text-white shadow-sm font-semibold'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>
    </header>
  );
};
