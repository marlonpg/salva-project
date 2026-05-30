import { useEffect, useState } from "react";

export default function LoginPage({ onSuccess, apiBase }) {
  const params = new URLSearchParams(window.location.search);
  const magicEmail = params.get("verifyEmail") || "";
  const magicCode = params.get("code") || "";

  const [mode, setMode] = useState(magicEmail && magicCode ? "verifying" : "request");
  const [email, setEmail] = useState(magicEmail);
  const [code, setCode] = useState(magicCode);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const authApiBase = apiBase.replace("/transport-requests", "");

  useEffect(() => {
    if (mode === "verifying") {
      autoVerify();
    }
  }, []);

  async function autoVerify() {
    try {
      const response = await fetch(`${authApiBase}/auth/verify-code`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: magicEmail.trim(), code: magicCode.trim() })
      });

      if (!response.ok) {
        setMode("verify");
        setError("Link inválido ou expirado. Insira o código manualmente.");
        return;
      }

      const data = await response.json();
      localStorage.setItem("sessionToken", data.token);
      window.history.replaceState({}, "", window.location.pathname);
      onSuccess();
    } catch {
      setMode("verify");
      setError("Erro ao verificar. Insira o código manualmente.");
    }
  }

  async function handleRequestAccess(e) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await fetch(`${authApiBase}/auth/request`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim() })
      });

      if (!response.ok) throw new Error("Failed to request access");
      setMessage("Solicitação enviada! Você receberá um link por email assim que for aprovado.");
      setMode("verify");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyCode(e) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await fetch(`${authApiBase}/auth/verify-code`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim(), code: code.trim() })
      });

      if (!response.ok) throw new Error("Invalid or expired code");
      const data = await response.json();
      localStorage.setItem("sessionToken", data.token);
      onSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="app-shell">
      <header className="hero">
        <h1>Acesso ao Sistema</h1>
        <p>Digite seu email para solicitar acesso</p>
      </header>

      {mode === "verifying" ? (
        <section className="table-section" style={{ maxWidth: "400px", margin: "2rem auto", textAlign: "center" }}>
          <p>Verificando seu acesso...</p>
        </section>
      ) : mode === "request" ? (
        <section className="table-section" style={{ maxWidth: "400px", margin: "2rem auto" }}>
          <form className="editor-form" onSubmit={handleRequestAccess}>
            <h2>Solicitar Acesso</h2>
            <input
              type="email"
              placeholder="seu@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            {error && <p className="state-message error">{error}</p>}
            {message && <p className="state-message" style={{ color: "#0f8d66" }}>{message}</p>}
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "Enviando..." : "Enviar Código"}
            </button>
          </form>
        </section>
      ) : (
        <section className="table-section" style={{ maxWidth: "400px", margin: "2rem auto" }}>
          <form className="editor-form" onSubmit={handleVerifyCode}>
            <h2>Verificar Código</h2>
            <input
              type="text"
              placeholder="123456"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              required
              maxLength="6"
            />
            {error && <p className="state-message error">{error}</p>}
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "Verificando..." : "Verificar"}
            </button>
            <button
              type="button"
              className="btn"
              onClick={() => setMode("request")}
              disabled={loading}
              style={{ marginTop: "0.5rem" }}
            >
              Voltar
            </button>
          </form>
        </section>
      )}
    </main>
  );
}
