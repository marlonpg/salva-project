#!/usr/bin/env python3
import csv
import requests
import re
import sys
from datetime import datetime

# Force UTF-8 output encoding
if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

API_BASE = "http://localhost:8080/api/expenses"
CSV_PATH = "sheets-docs/despesas.csv"

# Category mapping from CSV to ExpenseCategory enum
CATEGORY_MAP = {
    "seguro-2026": "SEGURO",
    "seguro": "SEGURO",
    "ipva": "IPVA",
    "IPVA": "IPVA",
    "oxigênio": "OXIGENIO",
    "gasolina": "GASOLINA",
    "medicamentos": "MEDICAMENTOS",
    "medicamento": "MEDICAMENTOS",
    "limpeza": "LIMPEZA",
    "celular": "CELULAR",
    "plano celular": "CELULAR",
    "contador": "CONTADOR",
    "contabilizei": "CONTADOR",
    "materiais": "MATERIAIS",
}

def parse_currency(value):
    """Convert R$xxx,xx to decimal number"""
    if not value or not isinstance(value, str):
        return 0.0
    # Remove R$ and spaces
    value = value.replace("R$", "").strip()
    # Replace comma with dot for decimal
    value = value.replace(",", ".")
    try:
        return float(value)
    except:
        return 0.0

def parse_date(date_str):
    """Convert dd-MM-yyyy to yyyy-MM-dd"""
    if not date_str or not isinstance(date_str, str):
        return None
    try:
        dt = datetime.strptime(date_str.strip(), "%d-%m-%Y")
        return dt.strftime("%Y-%m-%d")
    except:
        return None

def parse_pago(value):
    """Convert SIM/NÃO to boolean"""
    return str(value).strip().upper() == "SIM"

def map_category(value):
    """Map CSV category to ExpenseCategory enum"""
    if not value:
        return "OUTRO"
    value = value.strip().lower()
    return CATEGORY_MAP.get(value, "OUTRO")

def map_tipo(value):
    """Map CSV tipo to ExpenseType enum"""
    if not value:
        return "UNICO"
    value = value.strip().upper()
    if value == "UNICO":
        return "UNICO"
    elif value == "MENSAL":
        return "MENSAL"
    elif value == "ANUAL":
        return "ANUAL"
    return "UNICO"

def import_despesas():
    """Read CSV and create expenses via API"""
    created = 0
    skipped = 0
    errors = []

    try:
        with open(CSV_PATH, 'r', encoding='utf-8') as f:
            reader = csv.reader(f)
            header = next(reader)  # Skip header

            for row_num, row in enumerate(reader, start=2):
                try:
                    # Parse row
                    pago = parse_pago(row[1])
                    referencia = row[2].strip() if row[2] else ""
                    requerente = row[3].strip() if row[3] else ""
                    categoria = map_category(row[4])
                    tipo = map_tipo(row[5])
                    valor = parse_currency(row[6])
                    data_lancamento = parse_date(row[8])

                    # Validate required fields
                    if not referencia or not data_lancamento or valor <= 0:
                        skipped += 1
                        continue

                    # Create expense
                    payload = {
                        "referencia": referencia,
                        "requerente": requerente if requerente else None,
                        "categoria": categoria,
                        "tipo": tipo,
                        "pago": pago,
                        "valor": valor,
                        "dataLancamento": data_lancamento
                    }

                    response = requests.post(API_BASE, json=payload)

                    if response.status_code in [200, 201]:
                        created += 1
                        print(f"[OK] Row {row_num}: {referencia} - {valor}")
                    else:
                        errors.append(f"Row {row_num}: {response.status_code} {response.text}")
                        print(f"[FAIL] Row {row_num}: {response.status_code}")

                except Exception as e:
                    errors.append(f"Row {row_num}: {str(e)}")
                    print(f"[FAIL] Row {row_num}: {str(e)}")

        print(f"\n=== Import Summary ===")
        print(f"Created: {created}")
        print(f"Skipped: {skipped}")
        if errors:
            print(f"Errors: {len(errors)}")
            for error in errors[:5]:  # Show first 5 errors
                print(f"  - {error}")

    except FileNotFoundError:
        print(f"Error: File not found: {CSV_PATH}")
    except Exception as e:
        print(f"Error: {str(e)}")

if __name__ == "__main__":
    import_despesas()
