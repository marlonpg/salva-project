# Despesas (Expenses) Feature

A new page for the vet administrator to track and manage all business expenses.
Replaces the current spreadsheet workflow.

---

## Data Model

### Expense

| Field | Type | Notes |
|---|---|---|
| `id` | Long (PK) | auto-generated |
| `referencia` | String | free-text title / label (e.g. "IPVA - 2026/1") |
| `requerente` | String (nullable) | the team member who paid on our behalf and needs reimbursement |
| `categoria` | Enum | see categories below |
| `tipo` | Enum | UNICO / MENSAL / ANUAL |
| `pago` | Boolean | paid (true) or pending (false) |
| `valor` | BigDecimal | amount in BRL |
| `data_lancamento` | LocalDate | posting/payment date |
| `created_at` | Instant | |
| `updated_at` | Instant | |

### ExpenseCategory (enum)
- GASOLINA
- IPVA
- MEDICAMENTOS
- SEGURO
- OXIGENIO
- LIMPEZA
- CELULAR
- CONTADOR
- MATERIAIS
- OUTRO (generic — user types the description freely in `referencia`)

### ExpenseType (enum)
- UNICO — one-time payment
- MENSAL — monthly recurring
- ANUAL — annual recurring

---

## Backend

### New files
- `Expense.java` — entity
- `ExpenseCategory.java` — enum
- `ExpenseType.java` — enum
- `ExpenseRepository.java`
- `ExpenseService.java`
- `ExpenseController.java` → `POST/GET/PUT/DELETE /api/expenses`
- `ExpensePayload.java` — request/response DTO

### Migration (changeset 005)
Table `expense`:
- id, referencia, requerente (nullable), categoria, tipo, pago, valor, data_lancamento, created_at, updated_at

---

## Frontend

### Navigation
Add a tab bar at the top of the app to switch between:
- **Solicitações** (existing transport requests page)
- **Despesas** (new page)

Keep it simple — a single `activePage` state drives which content is rendered (no router needed).

### Despesas Page Layout (mirrors existing page patterns)

#### Toolbar
- Text search (searches referencia + requerente)
- Category filter dropdown (all categories + "Todas")
- Paid status filter (Todos / Pago / Pendente)
- Date range filter (from / to)
- "Nova Despesa" button

#### Stats Bar
- Total de despesas (count)
- Valor total
- Total pago
- Total pendente

#### Table
Columns: Data | Referência | Requerente | Categoria | Tipo | Valor | Status (Pago?) | Actions

Actions (same `<details>/<summary>` dropdown pattern):
- Editar
- Marcar como pago / Marcar como pendente (toggle)
- Excluir

#### Create / Edit Modal
Fields:
- Referência (text input, required)
- Requerente (text input, optional — "Quem pagou?")
- Categoria (select dropdown — enum values in pt-BR)
- Tipo (select: Único / Mensal / Anual)
  - If Mensal → show "Quantos meses?" (number input, min 1)
  - If Anual → show "Quantos anos?" (number input, min 1)
- Valor (currency input)
- Data de lançamento (date input — first occurrence)
- Pago? (checkbox)

#### Recurring Generation Logic
When tipo is MENSAL or ANUAL and `occurrences > 1`, the backend generates N expense records on save:
- Each entry shares the same referencia, categoria, tipo, valor, requerente, pago
- `data_lancamento` is offset: +1 month per entry (MENSAL) or +1 year per entry (ANUAL)
- The referencia gets an auto-suffix: "IPVA - 2026" → "IPVA - 2026/1", "IPVA - 2026/2", … (matches existing spreadsheet convention)
- Each generated record is independent (can be edited/deleted individually after creation)

The backend `POST /api/expenses` accepts `occurrences` in the payload and returns the list of all created records.

---

## Implementation Notes
- Requerente is free text (no linking to team members — allows flexibility with typo variations)
- Recurring generation auto-appends `/1`, `/2`, etc. to match spreadsheet convention
