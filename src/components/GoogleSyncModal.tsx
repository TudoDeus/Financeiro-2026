import React, { useState } from 'react';
import { 
  FileSpreadsheet, 
  RefreshCw, 
  ExternalLink, 
  CheckCircle2, 
  AlertCircle, 
  X, 
  Download, 
  Database,
  ArrowRight,
  ShieldCheck,
  Layers
} from 'lucide-react';
import { GoogleSheetMeta, Transaction } from '../types';
import { DEFAULT_SPREADSHEET_ID, extractSpreadsheetId } from '../services/sheetsApi';

interface GoogleSyncModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: any | null;
  sheetMeta: GoogleSheetMeta | null;
  spreadsheetInput: string;
  setSpreadsheetInput: (val: string) => void;
  onConnectSpreadsheet: (sheetId: string) => void;
  onSyncData: () => void;
  onExportLocalToSheet: () => void;
  isSyncing: boolean;
  transactionsCount: number;
  onLogin: () => void;
  syncMessage: string | null;
  syncError: string | null;
}

export const GoogleSyncModal: React.FC<GoogleSyncModalProps> = ({
  isOpen,
  onClose,
  user,
  sheetMeta,
  spreadsheetInput,
  setSpreadsheetInput,
  onConnectSpreadsheet,
  onSyncData,
  onExportLocalToSheet,
  isSyncing,
  transactionsCount,
  onLogin,
  syncMessage,
  syncError
}) => {
  const [activeTab, setActiveTab] = useState<'status' | 'settings'>('status');

  if (!isOpen) return null;

  const currentId = sheetMeta?.spreadsheetId || extractSpreadsheetId(spreadsheetInput) || DEFAULT_SPREADSHEET_ID;
  const sheetUrl = `https://docs.google.com/spreadsheets/d/${currentId}/edit`;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div 
        id="google-sync-modal-dialog"
        className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]"
      >
        {/* Header */}
        <div className="p-5 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 rounded-xl bg-emerald-100 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400">
              <FileSpreadsheet className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900 dark:text-white">
                Sincronização com Google Sheets
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Conecte e sincronize suas planilhas de finanças em tempo real
              </p>
            </div>
          </div>
          <button
            id="close-sync-modal-btn"
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Selector */}
        <div className="flex border-b border-slate-200 dark:border-slate-800 px-5 pt-2">
          <button
            onClick={() => setActiveTab('status')}
            className={`pb-2.5 px-3 text-xs font-semibold border-b-2 transition ${
              activeTab === 'status'
                ? 'border-emerald-600 text-emerald-600 dark:text-emerald-400'
                : 'border-transparent text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'
            }`}
          >
            Status & Ações
          </button>
          <button
            onClick={() => setActiveTab('settings')}
            className={`pb-2.5 px-3 text-xs font-semibold border-b-2 transition ${
              activeTab === 'settings'
                ? 'border-emerald-600 text-emerald-600 dark:text-emerald-400'
                : 'border-transparent text-slate-500 hover:text-slate-700 dark:hover:text-slate-300'
            }`}
          >
            Vincular Planilha (URL ou ID)
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 overflow-y-auto">
          {/* Status Message or Error */}
          {syncError && (
            <div className="p-3.5 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 flex items-start space-x-3 text-rose-800 dark:text-rose-300 text-xs">
              <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold">Aviso:</span> {syncError}
              </div>
            </div>
          )}

          {syncMessage && (
            <div className="p-3.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 flex items-start space-x-3 text-emerald-800 dark:text-emerald-300 text-xs">
              <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
              <div>{syncMessage}</div>
            </div>
          )}

          {/* User Auth Status Banner */}
          {!user ? (
            <div className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700 space-y-3">
              <div className="flex items-center space-x-2 text-slate-900 dark:text-white font-semibold text-sm">
                <ShieldCheck className="w-4 h-4 text-emerald-600" />
                <span>Autenticação Google necessária</span>
              </div>
              <p className="text-xs text-slate-600 dark:text-slate-400">
                Para carregar dados ou enviar lançamentos para a planilha original do Google Sheets, faça login com sua conta do Google:
              </p>
              <button
                id="modal-google-signin-btn"
                onClick={onLogin}
                className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs rounded-xl shadow-sm transition flex items-center justify-center space-x-2"
              >
                <span>Conectar com Google Workspace</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          ) : null}

          {activeTab === 'status' ? (
            <div className="space-y-4">
              {/* Connected Sheet Card */}
              <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800/60 space-y-3">
                <div className="flex items-start justify-between">
                  <div className="space-y-1">
                    <span className="text-[11px] font-semibold uppercase tracking-wider text-emerald-600 dark:text-emerald-400">
                      Planilha Ativa
                    </span>
                    <h3 className="text-base font-bold text-slate-900 dark:text-white">
                      {sheetMeta?.title || 'Controle Financeiro 2026'}
                    </h3>
                    <p className="text-xs text-slate-500 font-mono break-all">
                      ID: {currentId}
                    </p>
                  </div>
                  <a
                    id="open-external-sheet-link"
                    href={sheetUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center space-x-1 text-xs text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 font-medium p-1.5 hover:bg-emerald-50 dark:hover:bg-emerald-950/40 rounded-lg transition"
                  >
                    <span>Abrir Planilha</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                </div>

                {/* Detected Tabs */}
                {sheetMeta?.sheets && sheetMeta.sheets.length > 0 && (
                  <div className="pt-2 border-t border-slate-100 dark:border-slate-700/60">
                    <div className="flex items-center space-x-2 text-xs text-slate-500 mb-2">
                      <Layers className="w-3.5 h-3.5" />
                      <span>Abas Detectadas na Planilha:</span>
                    </div>
                    <div className="flex flex-wrap gap-1.5">
                      {sheetMeta.sheets.map((s, idx) => (
                        <span
                          key={idx}
                          className="px-2.5 py-1 text-xs bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-lg font-medium border border-slate-200 dark:border-slate-600"
                        >
                          {s.title} {s.rowCount ? `(${s.rowCount} linhas)` : ''}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Sync Controls */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <button
                  id="trigger-pull-sheets-btn"
                  onClick={onSyncData}
                  disabled={isSyncing || !user}
                  className={`p-4 rounded-xl border text-left transition flex flex-col justify-between space-y-2 ${
                    !user 
                      ? 'opacity-60 bg-slate-50 border-slate-200 dark:bg-slate-800/40 cursor-not-allowed'
                      : 'bg-emerald-50/60 border-emerald-200 dark:bg-emerald-950/20 dark:border-emerald-800 hover:bg-emerald-100/60'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-emerald-800 dark:text-emerald-300">
                      Importar da Planilha
                    </span>
                    <RefreshCw className={`w-4 h-4 text-emerald-600 ${isSyncing ? 'animate-spin' : ''}`} />
                  </div>
                  <p className="text-xs text-slate-600 dark:text-slate-400">
                    Lê as linhas da planilha e atualiza seu painel no app.
                  </p>
                </button>

                <button
                  id="trigger-export-to-sheet-btn"
                  onClick={onExportLocalToSheet}
                  disabled={isSyncing || !user}
                  className={`p-4 rounded-xl border text-left transition flex flex-col justify-between space-y-2 ${
                    !user
                      ? 'opacity-60 bg-slate-50 border-slate-200 dark:bg-slate-800/40 cursor-not-allowed'
                      : 'bg-slate-50 border-slate-200 dark:bg-slate-800/60 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-slate-800 dark:text-slate-200">
                      Exportar Lançamentos
                    </span>
                    <Download className="w-4 h-4 text-slate-600 dark:text-slate-400" />
                  </div>
                  <p className="text-xs text-slate-600 dark:text-slate-400">
                    Envia {transactionsCount} lançamentos cadastrados para o Google Sheets.
                  </p>
                </button>
              </div>
            </div>
          ) : (
            /* Settings Tab */
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Link ou ID da Planilha Google Sheets
                </label>
                <div className="flex space-x-2">
                  <input
                    id="spreadsheet-url-input"
                    type="text"
                    value={spreadsheetInput}
                    onChange={(e) => setSpreadsheetInput(e.target.value)}
                    placeholder="https://docs.google.com/spreadsheets/d/1VTcCWEPcvNS8McqF2jFqUDF03SonBTY6Ak1pYHKpxw8/edit..."
                    className="flex-1 px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                  <button
                    id="apply-spreadsheet-id-btn"
                    onClick={() => onConnectSpreadsheet(spreadsheetInput)}
                    className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold transition"
                  >
                    Vincular
                  </button>
                </div>
                <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1.5">
                  Insira o link padrão da planilha financeira ou cole o ID de outra planilha.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700 space-y-2">
                <div className="flex items-center space-x-2 text-xs font-bold text-slate-800 dark:text-slate-200">
                  <Database className="w-4 h-4 text-emerald-600" />
                  <span>Planilha Padrão do Projeto</span>
                </div>
                <p className="text-xs text-slate-600 dark:text-slate-400 font-mono text-[11px] break-all">
                  1VTcCWEPcvNS8McqF2jFqUDF03SonBTY6Ak1pYHKpxw8
                </p>
                <button
                  id="reset-to-default-sheet-btn"
                  onClick={() => {
                    setSpreadsheetInput(DEFAULT_SPREADSHEET_ID);
                    onConnectSpreadsheet(DEFAULT_SPREADSHEET_ID);
                  }}
                  className="text-xs text-emerald-600 dark:text-emerald-400 font-semibold hover:underline"
                >
                  Restaurar Planilha Modelo Padrão
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/50 flex justify-end">
          <button
            id="close-sync-modal-footer-btn"
            onClick={onClose}
            className="px-5 py-2 rounded-xl text-xs font-semibold bg-slate-200 dark:bg-slate-700 text-slate-800 dark:text-slate-200 hover:bg-slate-300 dark:hover:bg-slate-600 transition"
          >
            Concluir
          </button>
        </div>
      </div>
    </div>
  );
};
