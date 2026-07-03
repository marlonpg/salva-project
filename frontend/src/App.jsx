import { useEffect, useMemo, useState, useRef } from "react";
import LoginPage from "./LoginPage";
import AdminPage from "./AdminPage";

const queryApiBase = new URLSearchParams(window.location.search).get("apiBase");
if (queryApiBase) {
  localStorage.setItem("apiBaseOverride", queryApiBase);
}

const API_BASE = queryApiBase
  || localStorage.getItem("apiBaseOverride")
  || `${window.location.origin}/api/transport-requests`;

const SESSION_TOKEN = localStorage.getItem("sessionToken");
const AUTHENTICATED = !!SESSION_TOKEN;

async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem("sessionToken");
  const headers = options.headers || {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    localStorage.removeItem("sessionToken");
    window.location.reload();
    throw new Error("Session expired");
  }

  return response;
}

const statusLabels = {
  DONE: "Concluido",
  PENDING_PAYMENT: "Pagamento pendente",
  PENDING_PAYMENT_CLIENT: "Pagamento pendente (cliente)",
  PENDING_PAYMENT_HOSPITAL: "Pagamento pendente (hospital)",
  PENDING_NF: "NF pendente",
  PENDING_NF_VET: "NF pendente (vet)"
};

const emptyForm = {
  id: "",
  status: "DONE",
  description: "",
  requester: "",
  requesterIdNumber: "",
  requesterEmail: "",
  serviceDate: "",
  amount: "",
  tax: "",
  team: [{ id: null, personName: "", role: "DRIVER", amount: "" }]
};

const roleOptions = ["DRIVER", "VETERINARIAN", "REQUESTER", "ASSISTANT"];
const statusOptions = [
  "DONE",
  "PENDING_PAYMENT",
  "PENDING_PAYMENT_CLIENT",
  "PENDING_PAYMENT_HOSPITAL",
  "PENDING_NF",
  "PENDING_NF_VET"
];

function money(value) {
  return Number(value || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatDate(dateText) {
  if (!dateText) {
    return "-";
  }
  const date = new Date(`${dateText}T00:00:00`);
  if (Number.isNaN(date.getTime())) {
    return dateText;
  }
  return date.toLocaleDateString("pt-BR");
}

function EditIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25Zm14.71-9.04a1.003 1.003 0 0 0 0-1.42l-2.5-2.5a1.003 1.003 0 0 0-1.42 0l-1.96 1.96 3.75 3.75 2.13-1.79Z" fill="currentColor" />
    </svg>
  );
}

function DeleteIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path d="M6 7h12l-1 14H7L6 7Zm3-3h6l1 2h4v2H4V6h4l1-2Zm1 6v8h2v-8h-2Zm4 0v8h2v-8h-2Z" fill="currentColor" />
    </svg>
  );
}

function MoreIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path d="M12 7a1.75 1.75 0 1 1 0-3.5A1.75 1.75 0 0 1 12 7Zm0 6.75a1.75 1.75 0 1 1 0-3.5 1.75 1.75 0 0 1 0 3.5Zm0 6.75a1.75 1.75 0 1 1 0-3.5 1.75 1.75 0 0 1 0 3.5Z" fill="currentColor" />
    </svg>
  );
}

const expenseCategories = ["GASOLINA", "IPVA", "MEDICAMENTOS", "SEGURO", "OXIGENIO", "LIMPEZA", "CELULAR", "CONTADOR", "MATERIAIS", "OUTRO"];
const expenseTypes = ["UNICO", "MENSAL", "ANUAL"];

const emptyExpenseForm = {
  id: "",
  referencia: "",
  requerente: "",
  categoria: "GASOLINA",
  tipo: "UNICO",
  pago: false,
  valor: "",
  dataLancamento: "",
  occurrences: 1
};

function DespesasPage({ apiBase }) {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [categoriaFilter, setCategoriaFilter] = useState("ALL");
  const [pagoFilter, setPagoFilter] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingDeleteId, setPendingDeleteId] = useState(null);
  const [form, setForm] = useState(emptyExpenseForm);

  const expenseApiBase = apiBase.replace("/transport-requests", "") + "/expenses";

  async function loadExpenses() {
    try {
      setLoading(true);
      setError("");
      const response = await fetchWithAuth(expenseApiBase);
      if (!response.ok) throw new Error("Falha ao carregar despesas");
      setExpenses(await response.json());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadExpenses();
  }, []);

  const filteredExpenses = useMemo(() => {
    const term = search.trim().toLowerCase();
    return expenses
      .filter(exp => {
        const matchesStart = !startDate || (exp.dataLancamento && exp.dataLancamento >= startDate);
        const matchesEnd = !endDate || (exp.dataLancamento && exp.dataLancamento <= endDate);
        const matchesCategoria = categoriaFilter === "ALL" || exp.categoria === categoriaFilter;
        const matchesPago = pagoFilter === "ALL" || (pagoFilter === "SIM" ? exp.pago : !exp.pago);
        const matchesText = !term || exp.referencia.toLowerCase().includes(term) || (exp.requerente || "").toLowerCase().includes(term);
        return matchesStart && matchesEnd && matchesCategoria && matchesPago && matchesText;
      })
      .sort((a, b) => (a.dataLancamento || "").localeCompare(b.dataLancamento || "") * -1);
  }, [expenses, search, categoriaFilter, pagoFilter, startDate, endDate]);

  const totals = useMemo(() => ({
    count: filteredExpenses.length,
    valor: filteredExpenses.reduce((sum, exp) => sum + Number(exp.valor || 0), 0),
    pago: filteredExpenses.filter(exp => exp.pago).reduce((sum, exp) => sum + Number(exp.valor || 0), 0),
    pendente: filteredExpenses.filter(exp => !exp.pago).reduce((sum, exp) => sum + Number(exp.valor || 0), 0)
  }), [filteredExpenses]);

  function openNewModal() {
    setForm(emptyExpenseForm);
    setModalOpen(true);
  }

  function openEditModal(exp) {
    setForm({
      id: exp.id ?? "",
      referencia: exp.referencia ?? "",
      requerente: exp.requerente ?? "",
      categoria: exp.categoria ?? "GASOLINA",
      tipo: exp.tipo ?? "UNICO",
      pago: exp.pago ?? false,
      valor: exp.valor ?? "",
      dataLancamento: exp.dataLancamento ?? "",
      occurrences: 1
    });
    setModalOpen(true);
  }

  async function saveExpense() {
    const method = form.id ? "PUT" : "POST";
    const url = form.id ? `${expenseApiBase}/${form.id}` : expenseApiBase;

    try {
      const response = await fetchWithAuth(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form)
      });

      if (!response.ok) throw new Error("Falha ao salvar despesa");
      setModalOpen(false);
      await loadExpenses();
    } catch (err) {
      alert(err.message);
    }
  }

  function togglePago(id) {
    const exp = expenses.find(e => e.id === id);
    if (exp) {
      saveExpenseToggle(id, !exp.pago);
    }
  }

  async function saveExpenseToggle(id, newPago) {
    const exp = expenses.find(e => e.id === id);
    if (!exp) return;

    try {
      const response = await fetch(`${expenseApiBase}/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...exp, pago: newPago, occurrences: null })
      });

      if (!response.ok) throw new Error("Falha ao atualizar");
      await loadExpenses();
    } catch (err) {
      alert(err.message);
    }
  }

  function openDeleteConfirm(id) {
    setPendingDeleteId(id);
    setConfirmOpen(true);
  }

  async function confirmDelete() {
    try {
      const response = await fetch(`${expenseApiBase}/${pendingDeleteId}`, { method: "DELETE" });
      if (!response.ok) throw new Error("Falha ao excluir");
      setConfirmOpen(false);
      setPendingDeleteId(null);
      await loadExpenses();
    } catch (err) {
      alert(err.message);
    }
  }

  return (
    <>
      <section className="toolbar">
        <input type="search" placeholder="Buscar por referência ou requerente" value={search} onChange={(e) => setSearch(e.target.value)} />
        <select value={categoriaFilter} onChange={(e) => setCategoriaFilter(e.target.value)}>
          <option value="ALL">Todas as categorias</option>
          {expenseCategories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
        </select>
        <select value={pagoFilter} onChange={(e) => setPagoFilter(e.target.value)}>
          <option value="ALL">Todos</option>
          <option value="SIM">Pago</option>
          <option value="NAO">Pendente</option>
        </select>
        <button className="btn btn-primary" onClick={openNewModal}>Nova Despesa</button>
      </section>

      <section className="period-filters">
        <label>Data inicial <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} /></label>
        <label>Data final <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} /></label>
      </section>

      <section className="stats">
        <div>Total de despesas: <strong>{totals.count}</strong></div>
        <div>Valor total: <strong>{money(totals.valor)}</strong></div>
        <div>Total pago: <strong>{money(totals.pago)}</strong></div>
        <div>Total pendente: <strong>{money(totals.pendente)}</strong></div>
      </section>

      {error && <div style={{ color: "red", padding: "1rem" }}>{error}</div>}
      {loading && <div style={{ textAlign: "center", padding: "2rem" }}>Carregando...</div>}

      {!loading && (
        <section className="table-section">
          {filteredExpenses.length ? (
            <table className="records-table">
              <thead>
                <tr><th>Data</th><th>Referência</th><th>Requerente</th><th>Categoria</th><th>Tipo</th><th>Valor</th><th>Status</th><th>Ações</th></tr>
              </thead>
              <tbody>
                {filteredExpenses.map(exp => (
                  <tr key={exp.id}>
                    <td>{formatDate(exp.dataLancamento)}</td>
                    <td className="description-cell">{exp.referencia}</td>
                    <td>{exp.requerente || "-"}</td>
                    <td>{exp.categoria}</td>
                    <td>{exp.tipo}</td>
                    <td>{money(exp.valor)}</td>
                    <td><span className="tag">{exp.pago ? "Pago" : "Pendente"}</span></td>
                    <td>
                      <details className="row-menu">
                        <summary className="btn icon-btn menu-trigger" aria-label={`Mais ações para despesa #${exp.id}`} title="Mais ações">
                          <MoreIcon />
                        </summary>
                        <div className="row-menu-panel">
                          <button className="row-menu-item" type="button" onClick={() => openEditModal(exp)}>
                            <EditIcon />
                            <span>Editar</span>
                          </button>
                          <button className="row-menu-item" type="button" onClick={() => togglePago(exp.id)}>
                            <EditIcon />
                            <span>{exp.pago ? "Marcar pendente" : "Marcar pago"}</span>
                          </button>
                          <button className="row-menu-item danger-text" type="button" onClick={() => openDeleteConfirm(exp.id)}>
                            <DeleteIcon />
                            <span>Excluir</span>
                          </button>
                        </div>
                      </details>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="state-message">Nenhuma despesa encontrada.</p>
          )}
        </section>
      )}

      {modalOpen && (
        <div className="modal-backdrop" onClick={() => !Object.values(form).some(v => v) || setModalOpen(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <form className="editor-form" onSubmit={(e) => { e.preventDefault(); saveExpense(); }}>
              <h2>{form.id ? "Editar" : "Nova"} Despesa</h2>
              <input type="text" placeholder="Referência" value={form.referencia} onChange={(e) => setForm({...form, referencia: e.target.value})} required />
              <input type="text" placeholder="Requerente (opcional)" value={form.requerente} onChange={(e) => setForm({...form, requerente: e.target.value})} />
              <select value={form.categoria} onChange={(e) => setForm({...form, categoria: e.target.value})} required>
                {expenseCategories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
              </select>
              <select value={form.tipo} onChange={(e) => setForm({...form, tipo: e.target.value})} required>
                {expenseTypes.map(type => <option key={type} value={type}>{type}</option>)}
              </select>
              {form.tipo !== "UNICO" && (
                <input type="number" min="1" placeholder={form.tipo === "MENSAL" ? "Quantos meses?" : "Quantos anos?"} value={form.occurrences} onChange={(e) => setForm({...form, occurrences: parseInt(e.target.value) || 1})} />
              )}
              <input type="number" step="0.01" placeholder="Valor" value={form.valor} onChange={(e) => setForm({...form, valor: e.target.value})} required />
              <input type="date" value={form.dataLancamento} onChange={(e) => setForm({...form, dataLancamento: e.target.value})} required />
              <label><input type="checkbox" checked={form.pago} onChange={(e) => setForm({...form, pago: e.target.checked})} /> Pago?</label>
              <button type="submit" className="btn btn-primary">Salvar</button>
              <button type="button" className="btn" onClick={() => setModalOpen(false)}>Cancelar</button>
            </form>
          </div>
        </div>
      )}

      {confirmOpen && (
        <div className="modal-backdrop" onClick={() => setConfirmOpen(false)}>
          <div className="confirm-card" onClick={(e) => e.stopPropagation()}>
            <p>Tem certeza que deseja excluir esta despesa?</p>
            <button className="btn btn-primary" onClick={confirmDelete}>Excluir</button>
            <button className="btn" onClick={() => setConfirmOpen(false)}>Cancelar</button>
          </div>
        </div>
      )}
    </>
  );
}

function normalizeForm(row) {
  return {
    id: row?.id ?? "",
    status: row?.status ?? "DONE",
    description: row?.description ?? "",
    requester: row?.requester ?? "",
    requesterIdNumber: row?.requesterIdNumber ?? "",
    requesterEmail: row?.requesterEmail ?? "",
    serviceDate: row?.serviceDate ?? "",
    amount: row?.amount ?? "",
    tax: row?.tax ?? "",
    team: row?.team?.length
      ? row.team.map((member) => ({
          id: member.id ?? null,
          personName: member.personName ?? "",
          role: member.role ?? "DRIVER",
          amount: member.amount ?? ""
        }))
      : [{ id: null, personName: "", role: "DRIVER", amount: "" }]
  };
}

export default function App() {
  const [activePage, setActivePage] = useState(() =>
    localStorage.getItem("activePage") || "solicitacoes"
  );
  const [currentUser, setCurrentUser] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [memberFilter, setMemberFilter] = useState("ALL");
  const [modalOpen, setModalOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingDeleteId, setPendingDeleteId] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [importLoading, setImportLoading] = useState(false);
  const [importResult, setImportResult] = useState(null);
  const fileInputRef = useRef(null);

  async function loadRows() {
    try {
      setLoading(true);
      setError("");
      const response = await fetchWithAuth(API_BASE);
      if (!response.ok) {
        throw new Error("Falha ao carregar solicitacoes de transporte");
      }
      const data = await response.json();
      setRows(data);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleImportSheet(file) {
    if (!file) return;
    setImportLoading(true);
    setImportResult(null);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const authApiBase = API_BASE.replace("/transport-requests", "");
      const response = await fetchWithAuth(`${authApiBase}/import/sheet`, {
        method: "POST",
        body: formData
      });

      if (!response.ok) throw new Error("Falha ao importar arquivo");
      const result = await response.json();
      setImportResult(result);
      await loadRows();
    } catch (err) {
      setImportResult({ imported: 0, skipped: 0, errors: [err.message] });
    } finally {
      setImportLoading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  useEffect(() => {
    loadRows();
  }, []);

  useEffect(() => {
    localStorage.setItem("activePage", activePage);
  }, [activePage]);

  useEffect(() => {
    const token = localStorage.getItem("sessionToken");
    if (!token) {
      return;
    }

    const authApiBase = API_BASE.replace("/transport-requests", "");
    fetchWithAuth(`${authApiBase}/auth/validate`)
      .then(r => r.json())
      .then(data => {
        setCurrentUser(data.email);
        setIsAdmin(["gambadeveloper@gmail.com"].includes(data.email.toLowerCase()));
      })
      .catch(() => {
        localStorage.removeItem("sessionToken");
        window.location.reload();
      });
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      const openMenus = document.querySelectorAll('details.row-menu[open]');
      openMenus.forEach(menu => {
        if (!menu.contains(event.target)) {
          menu.open = false;
        }
      });
    }

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  const memberOptions = useMemo(() => {
    const names = rows.flatMap((row) => (row.team || []).map((member) => member.personName?.trim() || ""));
    return [...new Set(names.filter(Boolean))].sort((a, b) => a.localeCompare(b, "pt-BR"));
  }, [rows]);

  const filteredRows = useMemo(() => {
    const term = search.trim().toLowerCase();
    return [...rows]
      .filter((row) => {
        const date = row.serviceDate || "";
        const matchesStart = !startDate || (date && date >= startDate);
        const matchesEnd = !endDate || (date && date <= endDate);
        const normalizedMemberFilter = memberFilter.trim().toLowerCase();
        const matchesMember = memberFilter === "ALL"
          || (row.team || []).some((member) => (member.personName || "").trim().toLowerCase() === normalizedMemberFilter);
        const matchesText = !term
          || row.description.toLowerCase().includes(term)
          || row.requester.toLowerCase().includes(term)
          || (row.requesterIdNumber || "").toLowerCase().includes(term)
          || (row.requesterEmail || "").toLowerCase().includes(term);
        const matchesStatus = statusFilter === "ALL" || row.status === statusFilter;
        return matchesText && matchesStatus && matchesStart && matchesEnd && matchesMember;
      })
      .sort((a, b) => {
        const aDate = a.serviceDate || "";
        const bDate = b.serviceDate || "";
        if (aDate && bDate) {
          return bDate.localeCompare(aDate);
        }
        if (aDate) return -1;
        if (bDate) return 1;
        return Number(b.id || 0) - Number(a.id || 0);
      });
  }, [rows, search, statusFilter, startDate, endDate, memberFilter]);

  const memberPaidTotal = useMemo(() => {
    if (memberFilter === "ALL") {
      return 0;
    }

    const normalizedMemberFilter = memberFilter.trim().toLowerCase();
    return filteredRows.reduce((sum, row) => sum + (row.team || [])
      .filter((member) => (member.personName || "").trim().toLowerCase() === normalizedMemberFilter)
      .reduce((memberSum, member) => memberSum + Number(member.amount || 0), 0), 0);
  }, [filteredRows, memberFilter]);

  const token = localStorage.getItem("sessionToken");
  if (!token) {
    return <LoginPage onSuccess={() => window.location.reload()} apiBase={API_BASE} />;
  }

  const totals = useMemo(() => ({
    count: filteredRows.length,
    amount: filteredRows.reduce((sum, row) => sum + Number(row.amount || 0), 0),
    tax: filteredRows.reduce((sum, row) => sum + Number(row.tax || 0), 0)
  }), [filteredRows]);

  function openNewModal() {
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEditModal(row) {
    setForm(normalizeForm(row));
    setModalOpen(true);
  }

  function closeModal() {
    if (saving) {
      return;
    }
    setModalOpen(false);
  }

  function openDeleteConfirm(id) {
    setPendingDeleteId(id);
    setConfirmOpen(true);
  }

  function closeDeleteConfirm() {
    if (deleting) {
      return;
    }
    setConfirmOpen(false);
    setPendingDeleteId(null);
  }

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function updateTeamMember(index, field, value) {
    setForm((current) => ({
      ...current,
      team: current.team.map((member, memberIndex) => memberIndex === index ? { ...member, [field]: value } : member)
    }));
  }

  function addTeamMember() {
    setForm((current) => ({
      ...current,
      team: [...current.team, { id: null, personName: "", role: "DRIVER", amount: "" }]
    }));
  }

  function removeTeamMember(index) {
    setForm((current) => ({
      ...current,
      team: current.team.length === 1
        ? [{ id: null, personName: "", role: "DRIVER", amount: "" }]
        : current.team.filter((_, memberIndex) => memberIndex !== index)
    }));
  }

  async function saveRow(event) {
    event.preventDefault();
    try {
      setSaving(true);
      const payload = {
        status: form.status,
        description: form.description.trim(),
        requester: form.requester.trim(),
        requesterIdNumber: form.requesterIdNumber.trim(),
        requesterEmail: form.requesterEmail.trim(),
        serviceDate: form.serviceDate,
        amount: Number(form.amount),
        tax: Number(form.tax),
        team: form.team
          .filter((member) => member.personName.trim())
          .map((member) => ({
            id: member.id,
            personName: member.personName.trim(),
            role: member.role,
            amount: Number(member.amount)
          }))
      };

      const isEdit = Boolean(form.id);
      const response = await fetchWithAuth(isEdit ? `${API_BASE}/${form.id}` : API_BASE, {
        method: isEdit ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Falha ao salvar");
      }

      setModalOpen(false);
      await loadRows();
    } catch (saveError) {
      alert(`Falha ao salvar: ${saveError.message}`);
    } finally {
      setSaving(false);
    }
  }

  async function confirmDelete() {
    if (!pendingDeleteId) {
      return;
    }

    try {
      setDeleting(true);
      const response = await fetchWithAuth(`${API_BASE}/${pendingDeleteId}`, { method: "DELETE" });
      if (!response.ok) {
        alert("Falha ao excluir.");
        return;
      }

      closeDeleteConfirm();
      await loadRows();
    } finally {
      setDeleting(false);
    }
  }

  return (
    <main className="app-shell">
      <header className="hero">
        <button className="btn" style={{ position: "absolute", top: "1rem", right: "1rem" }} onClick={() => {
          localStorage.removeItem("sessionToken");
          window.location.reload();
        }}>Sair ({currentUser})</button>
        <h1>{activePage === "solicitacoes" ? "Solicitacoes de Transporte" : activePage === "despesas" ? "Despesas" : "Admin"}</h1>
        <p>{activePage === "solicitacoes" ? "Acompanhe, edite e revise solicitacoes no desktop e no celular." : activePage === "despesas" ? "Gerencie todas as despesas do negócio." : "Gerencie solicitações de acesso"}</p>
        <nav className="tab-nav">
          <button className={activePage === "solicitacoes" ? "active" : ""} onClick={() => setActivePage("solicitacoes")}>Solicitações</button>
          <button className={activePage === "despesas" ? "active" : ""} onClick={() => setActivePage("despesas")}>Despesas</button>
          {isAdmin && <button className={activePage === "admin" ? "active" : ""} onClick={() => setActivePage("admin")}>Admin</button>}
        </nav>
      </header>

      {activePage === "solicitacoes" ? (
        <>


      <section className="toolbar">
        <input
          type="search"
          placeholder="Buscar por descricao ou requerente"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
        <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
          <option value="ALL">Todos os status</option>
          {statusOptions.map((status) => (
            <option key={status} value={status}>{statusLabels[status]}</option>
          ))}
        </select>
        <button className="btn btn-primary" type="button" onClick={openNewModal}>Nova</button>
        {isAdmin && (
          <>
            <input
              ref={fileInputRef}
              type="file"
              accept=".csv"
              style={{ display: "none" }}
              onChange={(e) => e.target.files?.[0] && handleImportSheet(e.target.files[0])}
            />
            <button className="btn" type="button" onClick={() => fileInputRef.current?.click()} disabled={importLoading}>
              {importLoading ? "Importando..." : "Importar CSV"}
            </button>
          </>
        )}
      </section>

      {importResult && (
        <div className="state-message" style={{ backgroundColor: importResult.errors?.length ? "#fef2f2" : "#f0fdf4" }}>
          <strong>Importação:</strong> {importResult.imported} criadas, {importResult.skipped} puladas
          {importResult.errors?.length > 0 && (
            <details style={{ marginTop: "0.5rem", cursor: "pointer" }}>
              <summary>{importResult.errors.length} erro(s)</summary>
              <ul style={{ marginTop: "0.5rem", paddingLeft: "1.5rem" }}>
                {importResult.errors.slice(0, 5).map((err, i) => (
                  <li key={i} style={{ fontSize: "0.9rem", color: "#7f1d1d" }}>{err}</li>
                ))}
              </ul>
            </details>
          )}
        </div>
      )}

      <section className="period-filters">
        <label>
          Data inicial
          <input type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} />
        </label>
        <label>
          Data final
          <input type="date" value={endDate} onChange={(event) => setEndDate(event.target.value)} />
        </label>
        <label>
          Membro
          <select value={memberFilter} onChange={(event) => setMemberFilter(event.target.value)}>
            <option value="ALL">Todos os membros</option>
            {memberOptions.map((memberName) => (
              <option key={memberName} value={memberName}>{memberName}</option>
            ))}
          </select>
        </label>
      </section>

      <section className="stats">
        <div className="stat"><span>Itens</span><b>{totals.count}</b></div>
        <div className="stat"><span>Valor total</span><b>{money(totals.amount)}</b></div>
        <div className="stat"><span>Imposto total</span><b>{money(totals.tax)}</b></div>
        <div className="stat">
          <span>{memberFilter === "ALL" ? "Pago ao membro" : `Pago a ${memberFilter}`}</span>
          <b>{memberFilter === "ALL" ? "-" : money(memberPaidTotal)}</b>
        </div>
      </section>

      <section className="table-section">
        {loading ? <p className="state-message">Carregando...</p> : null}
        {!loading && error ? <p className="state-message error">{error}</p> : null}
        {!loading && !error && !filteredRows.length ? <p className="state-message">Nenhum registro encontrado.</p> : null}
        {!loading && !error && filteredRows.length ? (
          <table className="records-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Data</th>
                <th>Status</th>
                <th>Descricao</th>
                <th>Requerente</th>
                <th>Valor</th>
                <th>Imposto</th>
                <th>Acoes</th>
              </tr>
            </thead>
            <tbody>
              {filteredRows.map((row) => (
                <tr key={row.id}>
                  <td>#{row.id}</td>
                  <td>{formatDate(row.serviceDate)}</td>
                  <td><span className="tag">{statusLabels[row.status] || row.status}</span></td>
                  <td className="description-cell">{row.description}</td>
                  <td className="requester-cell">
                    <strong>{row.requester}</strong>
                    <span>{row.requesterIdNumber || "-"}</span>
                    <span>{row.requesterEmail || "-"}</span>
                  </td>
                  <td>{money(row.amount)}</td>
                  <td>{money(row.tax)}</td>
                  <td>
                    <details className="row-menu">
                      <summary className="btn icon-btn menu-trigger" aria-label={`Mais acoes para item #${row.id}`} title="Mais acoes">
                        <MoreIcon />
                      </summary>
                      <div className="row-menu-panel">
                        <button className="row-menu-item" type="button" onClick={() => openEditModal(row)}>
                          <EditIcon />
                          <span>Editar</span>
                        </button>
                        <button className="row-menu-item danger-text" type="button" onClick={() => openDeleteConfirm(row.id)}>
                          <DeleteIcon />
                          <span>Excluir</span>
                        </button>
                      </div>
                    </details>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : null}
      </section>

      {modalOpen ? (
        <div className="modal-backdrop" onClick={closeModal}>
          <div className="modal-card" onClick={(event) => event.stopPropagation()}>
            <form className="editor-form" onSubmit={saveRow}>
              <h2>{form.id ? `Editar #${form.id}` : "Nova Solicitacao"}</h2>

              <label>
                Status
                <select value={form.status} onChange={(event) => updateForm("status", event.target.value)} required>
                  {statusOptions.map((status) => (
                    <option key={status} value={status}>{statusLabels[status]}</option>
                  ))}
                </select>
              </label>

              <label>
                Descricao
                <input value={form.description} onChange={(event) => updateForm("description", event.target.value)} maxLength={500} required />
              </label>

              <label>
                Requerente
                <input value={form.requester} onChange={(event) => updateForm("requester", event.target.value)} required />
              </label>

              <div className="grid-2">
                <label>
                  CPF/RG/CNPJ
                  <input
                    value={form.requesterIdNumber}
                    onChange={(event) => updateForm("requesterIdNumber", event.target.value)}
                    maxLength={64}
                    required
                  />
                </label>
                <label>
                  Email
                  <input
                    type="email"
                    value={form.requesterEmail}
                    onChange={(event) => updateForm("requesterEmail", event.target.value)}
                    required
                  />
                </label>
              </div>

              <div className="grid-2">
                <label>
                  Data do servico
                  <input type="date" value={form.serviceDate} onChange={(event) => updateForm("serviceDate", event.target.value)} required />
                </label>
                <label>
                  Valor
                  <input type="number" min="0" step="0.01" value={form.amount} onChange={(event) => updateForm("amount", event.target.value)} required />
                </label>
              </div>

              <label>
                Imposto
                <input type="number" min="0" step="0.01" value={form.tax} onChange={(event) => updateForm("tax", event.target.value)} required />
              </label>

              <div className="team-block">
                <div className="team-header">
                  <h3>Equipe</h3>
                  <button className="btn" type="button" onClick={addTeamMember}>Adicionar membro</button>
                </div>
                <div className="team-rows">
                  {form.team.map((member, index) => (
                    <div className="team-row" key={`${member.id || "new"}-${index}`}>
                      <input
                        placeholder="Nome da pessoa"
                        value={member.personName}
                        onChange={(event) => updateTeamMember(index, "personName", event.target.value)}
                        required
                      />
                      <select value={member.role} onChange={(event) => updateTeamMember(index, "role", event.target.value)}>
                        {roleOptions.map((role) => <option key={role} value={role}>{role}</option>)}
                      </select>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        value={member.amount}
                        onChange={(event) => updateTeamMember(index, "amount", event.target.value)}
                        required
                      />
                      <button className="btn" type="button" onClick={() => removeTeamMember(index)}>Remover</button>
                    </div>
                  ))}
                </div>
              </div>

              <div className="form-actions">
                <button className="btn" type="button" onClick={closeModal}>Cancelar</button>
                <button className="btn btn-primary" type="submit" disabled={saving}>{saving ? "Salvando..." : "Salvar"}</button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {confirmOpen ? (
        <div className="modal-backdrop" onClick={closeDeleteConfirm}>
          <div className="modal-card confirm-card" onClick={(event) => event.stopPropagation()}>
            <div className="confirm-content">
              <h3>Confirmar exclusao</h3>
              <p>Deseja excluir o item #{pendingDeleteId}?</p>
              <div className="form-actions">
                <button className="btn" type="button" onClick={closeDeleteConfirm}>Cancelar</button>
                <button className="btn danger" type="button" onClick={confirmDelete} disabled={deleting}>
                  {deleting ? "Excluindo..." : "OK"}
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : null}
        </>
      ) : activePage === "despesas" ? (
        <DespesasPage apiBase={API_BASE} />
      ) : activePage === "admin" ? (
        <AdminPage apiBase={API_BASE} />
      ) : null}
    </main>
  );
}
