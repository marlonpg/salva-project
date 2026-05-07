I want you to build a new endpoint to import transports from a old spreadsheet.
For that we will need a new table probably to registry this imports and create a relation to the TransporteRequest table, so in case I try to reimport the sheet, it will identify and avoid duplication.
The sheets are inside /sheets-docs folder and that 


we should use the Nº column for idepotency only in this new import endpoint (so in case I reimport the same file it wont duplicate)
CONCLUIDO? = Status (in case you don't find any status put it as Concluido)
Nº = only means something for the new import table (but we could make a relationship with the current request, since we are going to generate a new line in TransportRequest table, I don't want to modify TransportRequest,, but we could add the Id from TransportRequest into TransportRequestImported (new table lets say) )
so we have the first Requerente which is always the VETERINARIAN name
And in the colum Veterinário is the value he received

The second Requerente is the DRIVER name
And inside Motorista is the value he received
the other columns you should be able to figure out

CONCLUIDO?,Nº,Descrição,Valor,Requerente,Veterinário,Extra,Requerente,Motorista,Mês,Data,Resultado,Imposto,Nota Fiscal Serviço,Nota Fiscal Vet
OK,1,Filó,"R$1,500.00",Vitória/Rafa,R$500.00,R$22.00,Marlon,R$100.00,dezembro,30-12-2025,R$788.00,R$90.00,,


