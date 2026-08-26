import React, { useState } from 'react';
import { 
  X, 
  Smartphone, 
  Download, 
  ExternalLink, 
  Copy, 
  Check, 
  Share2, 
  Sparkles, 
  ShieldCheck, 
  Layers,
  ArrowRight
} from 'lucide-react';

interface InstallAppModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const InstallAppModal: React.FC<InstallAppModalProps> = ({
  isOpen,
  onClose
}) => {
  const [copied, setCopied] = useState(false);

  if (!isOpen) return null;

  const appUrl = window.location.origin || 'https://ais-pre-5vf4iur7ll3rfnvkkzgsic-580077304711.us-east1.run.app';

  const handleCopy = () => {
    navigator.clipboard.writeText(appUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
      <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-emerald-50/50 dark:bg-emerald-950/30">
          <div className="flex items-center space-x-2.5">
            <div className="w-9 h-9 rounded-xl bg-emerald-600 flex items-center justify-center text-white shadow-md shadow-emerald-600/30">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-slate-900 dark:text-white">
                Instalar Aplicativo no Celular (Android / APK)
              </h2>
              <p className="text-[11px] text-slate-500">
                Acesse como aplicativo nativo no seu smartphone
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

        {/* Modal Content */}
        <div className="p-6 space-y-5 overflow-y-auto flex-1">
          {/* Link Box */}
          <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 space-y-2">
            <span className="text-[11px] font-bold text-slate-500 uppercase">
              URL Oficial do Aplicativo (Sempre Ativa):
            </span>
            <div className="flex items-center space-x-2">
              <input
                type="text"
                readOnly
                value={appUrl}
                className="w-full px-3 py-1.5 text-xs font-mono font-medium rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-emerald-600 dark:text-emerald-400 select-all"
              />
              <button
                type="button"
                onClick={handleCopy}
                className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold flex items-center space-x-1 transition flex-shrink-0"
              >
                {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'Copiado!' : 'Copiar'}</span>
              </button>
            </div>
          </div>

          {/* Option 1: Direct Android PWA Installation */}
          <div className="p-4 rounded-xl border border-emerald-200 dark:border-emerald-800/60 bg-emerald-50/40 dark:bg-emerald-950/20 space-y-3">
            <div className="flex items-center space-x-2 text-emerald-900 dark:text-emerald-200 font-bold text-xs">
              <Sparkles className="w-4 h-4 text-emerald-600" />
              <span>Opção 1: Instalação Instantânea no Android (Recomendada)</span>
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
              O aplicativo já está configurado como <strong>PWA nativo</strong> com ícone e tela cheia (sem barra de navegador).
            </p>
            <ol className="text-xs text-slate-700 dark:text-slate-300 space-y-1.5 list-decimal pl-4">
              <li>Abra o link acima no <strong>Google Chrome</strong> do seu celular Android.</li>
              <li>Toque no menu de <strong>3 pontinhos (⋮)</strong> no canto superior direito do Chrome.</li>
              <li>Selecione <strong>"Instalar aplicativo"</strong> ou <strong>"Adicionar à tela inicial"</strong>.</li>
              <li>O ícone do <strong>Controle Financeiro</strong> será adicionado à gaveta de apps e tela inicial do seu celular, funcionando como app nativo.</li>
            </ol>
          </div>

          {/* Option 2: Generate Signed APK via PWABuilder */}
          <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/30 space-y-3">
            <div className="flex items-center space-x-2 text-slate-900 dark:text-white font-bold text-xs">
              <Download className="w-4 h-4 text-indigo-600" />
              <span>Opção 2: Gerar pacote .APK instalador</span>
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              Você pode converter este link diretamente em um arquivo <strong>.apk</strong> assinado para Android usando a ferramenta oficial da Microsoft:
            </p>
            <div className="space-y-1.5 text-xs text-slate-600 dark:text-slate-300">
              <p>1. Acesse <strong>pwabuilder.com</strong></p>
              <p>2. Cole o link do seu app copiado acima</p>
              <p>3. Clique em <strong>"Generate Android Package (.APK / .AAB)"</strong></p>
            </div>
            <a
              href="https://www.pwabuilder.com"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold transition shadow-sm"
            >
              <span>Abrir PWABuilder</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-end bg-slate-50/50 dark:bg-slate-800/50">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold rounded-xl bg-slate-900 hover:bg-slate-800 text-white dark:bg-white dark:text-slate-900 transition"
          >
            Entendido
          </button>
        </div>
      </div>
    </div>
  );
};
