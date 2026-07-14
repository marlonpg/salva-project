#!/bin/bash
# Test Freemarker + Flying Saucer PDF Generator

echo "========================================="
echo "Testing Freemarker PDF Generator"
echo "========================================="
echo ""

# Test 1: Complete form with all fields
echo "Test 1: Generating PDF with ALL fields filled..."
curl -X POST http://localhost:8081/api/agreements/transport \
  -H "Content-Type: application/json" \
  -d '{
    "tutor": "João Silva Santos",
    "cpf": "123.456.789-00",
    "telefone": "(11) 98765-4321",
    "email": "joao.silva@example.com",
    "endereco": "Rua das Flores, 123, Apto 456",
    "bairro": "Centro",
    "cep": "01234-567",
    "cidade": "São Paulo",
    "paciente": "Rex",
    "especie": "Cachorro",
    "raca": "Labrador Retriever",
    "sexo": "Macho",
    "idade": "3 anos",
    "autorizaImagem": "S",
    "dataDoc": "13",
    "mesDoc": "Julho",
    "anoDoc": "2026"
  }' \
  -o teste_completo.pdf

echo ""
echo "✅ PDF generated: teste_completo.pdf"
echo ""

# Test 2: Partial form (only some fields)
echo "Test 2: Generating PDF with PARTIAL fields..."
curl -X POST http://localhost:8081/api/agreements/transport \
  -H "Content-Type: application/json" \
  -d '{
    "tutor": "Maria Santos",
    "cpf": "987.654.321-11",
    "paciente": "Fluffy",
    "especie": "Gato",
    "raca": "Persa"
  }' \
  -o teste_parcial.pdf

echo ""
echo "✅ PDF generated: teste_parcial.pdf"
echo ""

# Test 3: Check available fields
echo "Test 3: Listing available fields..."
curl -s http://localhost:8081/api/agreements/transport/fields | jq '.'

echo ""
echo "========================================="
echo "✅ All tests completed!"
echo "========================================="
echo ""
echo "Generated files:"
ls -lh teste_*.pdf 2>/dev/null
