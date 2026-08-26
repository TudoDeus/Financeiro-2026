export type TransactionType = 'income' | 'expense' | 'transfer';
export type TransactionStatus = 'completed' | 'pending';
export type AccountType = 'checking' | 'savings' | 'credit' | 'investment' | 'cash';

export interface Transaction {
  id: string;
  date: string; // YYYY-MM-DD
  description: string;
  amount: number;
  type: TransactionType;
  category: string;
  subcategory?: string;
  account: string;
  targetAccount?: string; // for transfers
  status: TransactionStatus;
  notes?: string;
  tags?: string[];
  spreadsheetRowIndex?: number;
  syncedWithSheet?: boolean;
  isRecurring?: boolean;
  recurrenceGroupId?: string;
  isInstallment?: boolean;
  installmentGroupId?: string;
  installmentCurrent?: number;
  installmentTotal?: number;
  originalAmount?: number;
}

export interface Category {
  id: string;
  name: string;
  type: 'income' | 'expense';
  icon: string;
  color: string;
  monthlyBudget: number;
}

export interface Account {
  id: string;
  name: string;
  type: AccountType;
  institution: string;
  balance: number;
  color: string;
  accountNumber?: string;
}

export interface CreditCard {
  id: string;
  name: string;
  institution: string;
  limit: number;
  currentInvoice: number;
  closingDay: number;
  dueDay: number;
  color: string;
}

export interface FinancialGoal {
  id: string;
  title: string;
  targetAmount: number;
  currentAmount: number;
  deadline: string; // YYYY-MM-DD
  category: string;
  color: string;
  notes?: string;
}

export interface MonthSummary {
  month: string; // "2026-01", etc.
  totalIncome: number;
  totalExpense: number;
  balance: number;
  savingsRate: number;
  pendingExpenses: number;
  pendingIncomes: number;
}

export interface GoogleSheetMeta {
  spreadsheetId: string;
  title: string;
  sheets: {
    sheetId: number;
    title: string;
    rowCount?: number;
    columnCount?: number;
  }[];
  lastSync?: string;
  isCustomUrl?: boolean;
}
