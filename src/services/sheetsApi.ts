import { Transaction, GoogleSheetMeta } from '../types';

export const DEFAULT_SPREADSHEET_ID = '1VTcCWEPcvNS8McqF2jFqUDF03SonBTY6Ak1pYHKpxw8';

export interface SheetRowData {
  range: string;
  majorDimension: string;
  values: string[][];
}

/**
 * Extracts spreadsheet ID from full Google Sheets URL or raw ID
 */
export function extractSpreadsheetId(input: string): string {
  if (!input) return DEFAULT_SPREADSHEET_ID;
  const clean = input.trim();
  const match = clean.match(/\/spreadsheets\/d\/([a-zA-Z0-9-_]+)/);
  if (match && match[1]) {
    return match[1];
  }
  // If user pasted just the ID
  if (/^[a-zA-Z0-9-_]{20,}$/.test(clean)) {
    return clean;
  }
  return DEFAULT_SPREADSHEET_ID;
}

/**
 * Fetch spreadsheet metadata (title and sheet tabs)
 */
export async function getSpreadsheetMeta(accessToken: string, spreadsheetId: string = DEFAULT_SPREADSHEET_ID): Promise<GoogleSheetMeta> {
  const response = await fetch(`https://sheets.googleapis.com/v4/spreadsheets/${spreadsheetId}`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error?.message || `Erro ao acessar planilha (${response.status}: ${response.statusText})`);
  }

  const data = await response.json();
  return {
    spreadsheetId,
    title: data.properties?.title || 'Controle Financeiro 2026',
    sheets: (data.sheets || []).map((s: { properties: { sheetId: number; title: string; gridProperties?: { rowCount?: number; columnCount?: number } } }) => ({
      sheetId: s.properties.sheetId,
      title: s.properties.title,
      rowCount: s.properties.gridProperties?.rowCount,
      columnCount: s.properties.gridProperties?.columnCount
    })),
    lastSync: new Date().toISOString()
  };
}

/**
 * Read raw sheet values by range
 */
export async function getSheetValues(
  accessToken: string,
  spreadsheetId: string,
  range: string
): Promise<string[][]> {
  const encodedRange = encodeURIComponent(range);
  const response = await fetch(
    `https://sheets.googleapis.com/v4/spreadsheets/${spreadsheetId}/values/${encodedRange}?valueRenderOption=FORMATTED_VALUE`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      }
    }
  );

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error?.message || `Erro ao ler dados da planilha (${response.status})`);
  }

  const data: SheetRowData = await response.json();
  return data.values || [];
}

/**
 * Parses raw 2D sheet rows into strongly-typed Transaction objects
 */
export function parseSheetRowsToTransactions(rows: string[][]): Transaction[] {
  if (!rows || rows.length < 2) return [];

  const headerRow = rows[0].map(h => (h || '').toString().trim().toLowerCase());
  
  // Find column indices heuristically
  const dateIdx = headerRow.findIndex(h => h.includes('data') || h.includes('date') || h.includes('dia') || h.includes('vencimento'));
  const descIdx = headerRow.findIndex(h => h.includes('descri') || h.includes('lançamento') || h.includes('item') || h.includes('nome') || h.includes('título') || h.includes('detalhe'));
  const amountIdx = headerRow.findIndex(h => h.includes('valor') || h.includes('preço') || h.includes('quantia') || h.includes('total') || h.includes('r$') || h.includes('amount'));
  const typeIdx = headerRow.findIndex(h => h.includes('tipo') || h.includes('type') || h.includes('natureza'));
  const categoryIdx = headerRow.findIndex(h => h.includes('categor') || h.includes('grupo') || h.includes('classifica'));
  const accountIdx = headerRow.findIndex(h => h.includes('conta') || h.includes('banco') || h.includes('forma') || h.includes('pagamento') || h.includes('cartão'));
  const statusIdx = headerRow.findIndex(h => h.includes('status') || h.includes('situa') || h.includes('pago') || h.includes('liquidado'));

  const parsedTransactions: Transaction[] = [];

  for (let i = 1; i < rows.length; i++) {
    const row = rows[i];
    if (!row || row.length === 0 || !row.some(cell => cell && cell.toString().trim() !== '')) {
      continue;
    }

    const rawDate = dateIdx !== -1 && row[dateIdx] ? row[dateIdx].trim() : '';
    const rawDesc = descIdx !== -1 && row[descIdx] ? row[descIdx].trim() : `Lançamento ${i}`;
    const rawAmount = amountIdx !== -1 && row[amountIdx] ? row[amountIdx] : '0';
    const rawType = typeIdx !== -1 && row[typeIdx] ? row[typeIdx].trim().toLowerCase() : '';
    const rawCategory = categoryIdx !== -1 && row[categoryIdx] ? row[categoryIdx].trim() : 'Geral';
    const rawAccount = accountIdx !== -1 && row[accountIdx] ? row[accountIdx].trim() : 'Conta Principal';
    const rawStatus = statusIdx !== -1 && row[statusIdx] ? row[statusIdx].trim().toLowerCase() : 'pago';

    // Parse amount to number
    let cleanAmountStr = rawAmount.toString().replace(/[R$\s.]/g, '').replace(',', '.');
    // If original had dot as decimal
    if (rawAmount.includes('.') && !rawAmount.includes(',')) {
      cleanAmountStr = rawAmount.toString().replace(/[R$\s]/g, '');
    }
    const numAmount = Math.abs(parseFloat(cleanAmountStr) || 0);

    // Determine type
    let type: 'income' | 'expense' | 'transfer' = 'expense';
    if (rawType.includes('rec') || rawType.includes('entr') || rawType.includes('ganho') || rawType.includes('salário') || rawType.includes('income')) {
      type = 'income';
    } else if (rawType.includes('transf') || rawType.includes('transfer')) {
      type = 'transfer';
    } else if (rawType.includes('desp') || rawType.includes('saíd') || rawType.includes('gasto') || rawType.includes('expense') || rawAmount.includes('-')) {
      type = 'expense';
    }

    // Format date to YYYY-MM-DD
    let formattedDate = new Date().toISOString().split('T')[0];
    if (rawDate) {
      if (rawDate.includes('/')) {
        const parts = rawDate.split('/');
        if (parts.length === 3) {
          const day = parts[0].padStart(2, '0');
          const month = parts[1].padStart(2, '0');
          let year = parts[2];
          if (year.length === 2) year = '20' + year;
          formattedDate = `${year}-${month}-${day}`;
        }
      } else if (rawDate.match(/^\d{4}-\d{2}-\d{2}$/)) {
        formattedDate = rawDate;
      }
    }

    const status = (rawStatus.includes('pend') || rawStatus.includes('abert') || rawStatus.includes('não') || rawStatus.includes('agend')) ? 'pending' : 'completed';

    if (numAmount > 0 || rawDesc) {
      parsedTransactions.push({
        id: `sheet-${i}-${Date.now().toString(36)}`,
        date: formattedDate,
        description: rawDesc,
        amount: numAmount,
        type,
        category: rawCategory || 'Outros',
        account: rawAccount || 'Conta Principal',
        status,
        spreadsheetRowIndex: i + 1,
        syncedWithSheet: true
      });
    }
  }

  return parsedTransactions;
}

/**
 * Append transaction row into a specific sheet tab in Google Sheets
 */
export async function appendTransactionToSheet(
  accessToken: string,
  spreadsheetId: string,
  sheetTitle: string,
  transaction: Transaction
): Promise<{ updatedRows: number }> {
  const formattedDate = transaction.date.split('-').reverse().join('/'); // DD/MM/YYYY
  const formattedAmount = transaction.amount.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const typeText = transaction.type === 'income' ? 'Receita' : transaction.type === 'expense' ? 'Despesa' : 'Transferência';
  const statusText = transaction.status === 'completed' ? 'Pago' : 'Pendente';

  const rowValues = [
    formattedDate,
    transaction.description,
    formattedAmount,
    transaction.category,
    transaction.account,
    typeText,
    statusText,
    transaction.notes || ''
  ];

  const range = `${sheetTitle}!A:H`;
  const response = await fetch(
    `https://sheets.googleapis.com/v4/spreadsheets/${spreadsheetId}/values/${encodeURIComponent(range)}:append?valueInputOption=USER_ENTERED&insertDataOption=INSERT_ROWS`,
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        values: [rowValues]
      })
    }
  );

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error?.message || `Erro ao registrar na planilha (${response.status})`);
  }

  const result = await response.json();
  return { updatedRows: result.updates?.updatedRows || 1 };
}
