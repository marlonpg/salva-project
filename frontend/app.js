const API_BASE = "http://localhost:8080/api/transport-requests";

const listEl = document.getElementById("list");
const statsEl = document.getElementById("stats");
const searchInput = document.getElementById("searchInput");
const statusFilter = document.getElementById("statusFilter");
const refreshBtn = document.getElementById("refreshBtn");
const newBtn = document.getElementById("newBtn");
const installBtn = document.getElementById("installBtn");

const editorDialog = document.getElementById("editorDialog");
const editorTitle = document.getElementById("editorTitle");
const editorForm = document.getElementById("editorForm");
const addTeamBtn = document.getElementById("addTeamBtn");
const teamRows = document.getElementById("teamRows");

let allRows = [];
let deferredInstallPrompt = null;

const money = (n) => Number(n || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const statusLabels = {
  DONE: "Concluido",
  PENDING_PAYMENT: "Pagamento pendente",
  PENDING_PAYMENT_CLIENT: "Pagamento pendente (cliente)",
  PENDING_PAYMENT_HOSPITAL: "Pagamento pendente (hospital)",
  PENDING_NF: "NF pendente",
  PENDING_NF_VET: "NF pendente (vet)"
};

async function fetchRows() {
  const response = await fetch(API_BASE);
  if (!response.ok) {
    throw new Error("Falha ao carregar solicitacoes de transporte");
  }
  allRows = await response.json();
  render();
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

function formatStatus(status) {
  return statusLabels[status] || status;
}

function getFilteredRows() {
  const term = searchInput.value.trim().toLowerCase();
  const status = statusFilter.value;
  return allRows.filter((row) => {
    const matchesText = !term
      || row.description.toLowerCase().includes(term)
      || row.requester.toLowerCase().includes(term);
    const matchesStatus = status === "ALL" || row.status === status;
    return matchesText && matchesStatus;
  });
}

function sortRowsByDateDesc(rows) {
  return [...rows].sort((a, b) => {
    const byDate = (b.serviceDate || "").localeCompare(a.serviceDate || "");
    if (byDate !== 0) {
      return byDate;
    }
    return Number(b.id || 0) - Number(a.id || 0);
  });
}

function renderStats(rows) {
  const totalAmount = rows.reduce((acc, row) => acc + Number(row.amount || 0), 0);
  const totalTax = rows.reduce((acc, row) => acc + Number(row.tax || 0), 0);
  statsEl.innerHTML = `
    <div class="stat"><span>Itens</span><b>${rows.length}</b></div>
    <div class="stat"><span>Valor total</span><b>${money(totalAmount)}</b></div>
    <div class="stat"><span>Imposto total</span><b>${money(totalTax)}</b></div>
  `;
}

function renderList(rows) {
  if (!rows.length) {
    listEl.innerHTML = "<p>Nenhum registro encontrado.</p>";
    return;
  }

  listEl.innerHTML = `
    <table class="records-table">
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
        ${rows.map((row) => `
          <tr>
            <td>#${row.id}</td>
            <td>${formatDate(row.serviceDate)}</td>
            <td><span class="tag">${formatStatus(row.status)}</span></td>
            <td class="description-cell">${escapeHtml(row.description)}</td>
            <td>${escapeHtml(row.requester)}</td>
            <td>${money(row.amount)}</td>
            <td>${money(row.tax)}</td>
            <td>
              <div class="actions">
                <button class="btn" data-edit="${row.id}">Editar</button>
                <button class="btn danger" data-delete="${row.id}">Excluir</button>
              </div>
            </td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;
}

function render() {
  const rows = sortRowsByDateDesc(getFilteredRows());
  renderStats(rows);
  renderList(rows);
}

function teamRowTemplate(member = {}) {
  return `
    <div class="team-row" data-team-id="${member.id || ""}">
      <input class="team-name" placeholder="Nome da pessoa" value="${escapeAttr(member.personName || "")}" required>
      <select class="team-role">
        ${["DRIVER", "VETERINARIAN", "REQUESTER", "ASSISTANT"].map((role) =>
          `<option value="${role}" ${member.role === role ? "selected" : ""}>${role}</option>`).join("")}
      </select>
      <input class="team-amount" type="number" step="0.01" min="0" value="${Number(member.amount || 0)}" required>
      <button type="button" class="btn team-remove">Remover</button>
    </div>
  `;
}

function openEditor(row) {
  const isEdit = Boolean(row);
  editorTitle.textContent = isEdit ? `Editar #${row.id}` : "Nova Solicitacao";

  document.getElementById("id").value = row?.id || "";
  document.getElementById("status").value = row?.status || "DONE";
  document.getElementById("description").value = row?.description || "";
  document.getElementById("requester").value = row?.requester || "";
  document.getElementById("serviceDate").value = row?.serviceDate || "";
  document.getElementById("amount").value = row?.amount ?? "";
  document.getElementById("tax").value = row?.tax ?? "";

  teamRows.innerHTML = (row?.team || []).map(teamRowTemplate).join("");
  if (!row?.team?.length) {
    teamRows.innerHTML = teamRowTemplate();
  }

  editorDialog.showModal();
}

function getPayloadFromForm() {
  const members = Array.from(teamRows.querySelectorAll(".team-row")).map((node) => ({
    id: node.dataset.teamId || null,
    personName: node.querySelector(".team-name").value.trim(),
    role: node.querySelector(".team-role").value,
    amount: Number(node.querySelector(".team-amount").value)
  })).filter((m) => m.personName);

  return {
    status: document.getElementById("status").value,
    description: document.getElementById("description").value.trim(),
    requester: document.getElementById("requester").value.trim(),
    serviceDate: document.getElementById("serviceDate").value,
    amount: Number(document.getElementById("amount").value),
    tax: Number(document.getElementById("tax").value),
    team: members
  };
}

async function saveForm(event) {
  event.preventDefault();
  const id = document.getElementById("id").value;
  const payload = getPayloadFromForm();

  const url = id ? `${API_BASE}/${id}` : API_BASE;
  const method = id ? "PUT" : "POST";

  const response = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const text = await response.text();
    alert(`Falha ao salvar: ${text}`);
    return;
  }

  editorDialog.close();
  await fetchRows();
}

async function deleteItem(id) {
  if (!confirm(`Excluir item #${id}?`)) {
    return;
  }

  const response = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
  if (!response.ok) {
    alert("Falha ao excluir.");
    return;
  }

  await fetchRows();
}

listEl.addEventListener("click", (event) => {
  const editId = event.target.getAttribute("data-edit");
  const deleteId = event.target.getAttribute("data-delete");

  if (editId) {
    const row = allRows.find((item) => String(item.id) === editId);
    openEditor(row);
  }

  if (deleteId) {
    deleteItem(deleteId);
  }
});

addTeamBtn.addEventListener("click", () => {
  teamRows.insertAdjacentHTML("beforeend", teamRowTemplate());
});

teamRows.addEventListener("click", (event) => {
  if (event.target.classList.contains("team-remove")) {
    event.target.closest(".team-row")?.remove();
  }
});

editorForm.addEventListener("submit", saveForm);
searchInput.addEventListener("input", render);
statusFilter.addEventListener("change", render);
refreshBtn.addEventListener("click", fetchRows);
newBtn.addEventListener("click", () => openEditor(null));

window.addEventListener("beforeinstallprompt", (event) => {
  event.preventDefault();
  deferredInstallPrompt = event;
  installBtn.hidden = false;
});

installBtn.addEventListener("click", async () => {
  if (!deferredInstallPrompt) {
    return;
  }

  deferredInstallPrompt.prompt();
  await deferredInstallPrompt.userChoice;
  deferredInstallPrompt = null;
  installBtn.hidden = true;
});

window.addEventListener("appinstalled", () => {
  deferredInstallPrompt = null;
  installBtn.hidden = true;
});

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
  return escapeHtml(value);
}

fetchRows().catch((err) => {
  listEl.innerHTML = `<p>${escapeHtml(err.message)}</p>`;
});

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("./service-worker.js").then((registration) => {
      registration.update();
    });
  });
}
