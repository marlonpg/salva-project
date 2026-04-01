#!/usr/bin/env bash

BASE_URL="http://localhost:8080/api/transport-requests"

curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DONE",
    "description": "Fil\u00f3",
    "requester": "",
    "serviceDate": "2025-12-30",
    "amount": 1500.00,
    "tax": 90.00,
    "team": [
      {
        "personName": "Vit\u00f3ria",
        "role": "VETERINARIAN",
        "amount": 250.00
      },
      {
        "personName": "Rafa",
        "role": "VETERINARIAN",
        "amount": 250.00
      },
      {
        "personName": "Marlon",
        "role": "DRIVER",
        "amount": 100.00
      }
    ]
  }'
