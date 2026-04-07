import { useEffect, useMemo, useState } from "react";

const queryApiBase = new URLSearchParams(window.location.search).get("apiBase");
if (queryApiBase) {
  localStorage.setItem("apiBaseOverride", queryApiBase);
}

const API_BASE = queryApiBase
  || localStorage.getItem("apiBaseOverride")
  || `${window.location.protocol}//${window.location.hostname}:8080/api/transport-requests`;

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

function normalizeForm(row) {
  return {
    id: row?.id ?? "",
    status: row?.status ?? "DONE",
    description: row?.description ?? "",
    requester: row?.requester ?? "",
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
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState(emptyForm);

  async function loadRows() {
    try {
      setLoading(true);
      setError("");
      const response = await fetch(API_BASE);
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

  useEffect(() => {
    loadRows();
  }, []);

  const filteredRows = useMemo(() => {
    const term = search.trim().toLowerCase();
    return [...rows]
      .filter((row) => {
        const matchesText = !term
          || row.description.toLowerCase().includes(term)
          || row.requester.toLowerCase().includes(term);
        const matchesStatus = statusFilter === "ALL" || row.status === statusFilter;
        return matchesText && matchesStatus;
      })
      .sort((a, b) => {
        const byDate = (b.serviceDate || "").localeCompare(a.serviceDate || "");
        if (byDate !== 0) {
          return byDate;
        }
        return Number(b.id || 0) - Number(a.id || 0);
      });
  }, [rows, search, statusFilter]);

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
      const response = await fetch(isEdit ? `${API_BASE}/${form.id}` : API_BASE, {
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

  async function deleteRow(id) {
    if (!window.confirm(`Excluir item #${id}?`)) {
      return;
    }

    const response = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
    if (!response.ok) {
      alert("Falha ao excluir.");
      return;
    }

    await loadRows();
  }

  return (
    <main className="app-shell">
      <header className="hero">
        <h1>Solicitacoes de Transporte</h1>
        <p>Acompanhe, edite e revise solicitacoes no desktop e no celular.</p>
      </header>

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
        <button className="btn" type="button" onClick={loadRows}>Atualizar</button>
      </section>

      <section className="stats">
        <div className="stat"><span>Itens</span><b>{totals.count}</b></div>
        <div className="stat"><span>Valor total</span><b>{money(totals.amount)}</b></div>
        <div className="stat"><span>Imposto total</span><b>{money(totals.tax)}</b></div>
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
                  <td>{row.requester}</td>
                  <td>{money(row.amount)}</td>
                  <td>{money(row.tax)}</td>
                  <td>
                    <div className="actions">
                      <button className="btn" type="button" onClick={() => openEditModal(row)}>Editar</button>
                      <button className="btn danger" type="button" onClick={() => deleteRow(row.id)}>Excluir</button>
                    </div>
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
    </main>
  );
}
