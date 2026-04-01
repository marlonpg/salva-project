$baseUrl = "http://localhost:8080/api/transport-requests"

$payload = @'
{
  "status": "DONE",
  "description": "Filó",
  "requester": "",
  "serviceDate": "2025-12-30",
  "amount": 1500.00,
  "tax": 90.00,
  "team": [
    {
      "personName": "Vitória",
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
}
'@

curl.exe -X POST $baseUrl -H "Content-Type: application/json" -d $payload
