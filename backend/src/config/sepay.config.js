import dotenv from 'dotenv';
dotenv.config();

export const BANK_CONFIG = {
  BANK_ID: process.env.BANK_ID,           // mã ngân hàng (VCB, MB, ...)
  ACCOUNT_NO: process.env.ACCOUNT_NO,     // stk
  TEMPLATE: "compact"                     // ui QR (compact, print, qr_only)
}