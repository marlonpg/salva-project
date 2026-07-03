import { useEffect, useState } from "react";

function formatDate(dateText) {
  if (!dateText) return "-";
  const date = new Date(dateText);
  if (Number.isNaN(date.getTime())) return dateText;
  return date.toLocaleDateString("pt-BR");
}

export default function AdminPage({ apiBase }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionLoading, setActionLoading] = useState(null);

  const sessionToken = localStorage.getItem("sessionToken");
  const authApiBase = apiBase.replace("/transport-requests", "");

  useEffect(() => {
    loadRequests();
  }, []);

  async function loadRequests() {
    try {
      setLoading(true);
      setError("");
      const response = await fetch(`${authApiBase}/admin/login-requests/history`, {
        headers: { "Authorization": `Bearer ${sessionToken}` }
      });

      if (!response.ok) throw new Error("Failed to load requests");
      setRequests(await response.json());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function approveRequest(id) {
    setActionLoading(id);
    try {
      const response = await fetch(`${authApiBase}/admin/login-requests/${id}/approve`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${sessionToken}` }
      });

      if (!response.ok) throw new Error("Failed to approve");
      await loadRequests();
    } catch (err) {
      alert(err.message);
    } finally {
      setActionLoading(null);
    }
  }

  async function rejectRequest(id) {
    if (!confirm("Deseja rejeitar esta solicitação?")) return;

    setActionLoading(id);
    try {
      const response = await fetch(`${authApiBase}/admin/login-requests/${id}/reject`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${sessionToken}` }
      });

      if (!response.ok) throw new Error("Failed to reject");
      await loadRequests();
    } catch (err) {
      alert(err.message);
    } finally {
      setActionLoading(null);
    }
  }

  return (
    <>
      <section className="toolbar">
        <h2 style={{ margin: 0 }}>Solicitações de Acesso</h2>
        <button className="btn" onClick={loadRequests}>Atualizar</button>
      </section>

      {error && <div className="state-message error">{error}</div>}
      {loading && <div style={{ textAlign: "center", padding: "2rem" }}>Carregando...</div>}

      {!loading && (
        <section className="table-section">
          {requests.length ? (
            <table className="records-table">
              <thead>
                <tr>
                  <th>Email</th>
                  <th>Código</th>
                  <th>Solicitado em</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {requests.map(req => (
                  <tr key={req.id}>
                    <td>{req.email}</td>
                    <td>{req.code || "-"}</td>
                    <td>{formatDate(req.requestedAt)}</td>
                    <td><span className="tag">{req.status}</span></td>
                    <td>
                      <div className="actions">
                        {req.status === "PENDING" && (
                          <>
                            <button
                              className="btn btn-primary"
                              onClick={() => approveRequest(req.id)}
                              disabled={actionLoading === req.id}
                              style={{ fontSize: "0.9rem", padding: "8px 12px" }}
                            >
                              {actionLoading === req.id ? "..." : "Aprovar"}
                            </button>
                            <button
                              className="btn danger"
                              onClick={() => rejectRequest(req.id)}
                              disabled={actionLoading === req.id}
                              style={{ fontSize: "0.9rem", padding: "8px 12px" }}
                            >
                              {actionLoading === req.id ? "..." : "Rejeitar"}
                            </button>
                          </>
                        )}
                        {req.status !== "PENDING" && (
                          <span style={{ color: "#5e7770", fontSize: "0.9rem" }}>
                            {req.status === "APPROVED" ? "✓ Aprovado" : "✗ Rejeitado"}
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="state-message">Nenhuma solicitação pendente</p>
          )}
        </section>
      )}
    </>
  );
}
