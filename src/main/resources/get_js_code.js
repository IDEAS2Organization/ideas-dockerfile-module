// En language manifest, la operación build contiene esta función después de pasarla por https://jscompress.com/
// Luego se sustituyen las comillas dobles por simples para evitar problemas de sintaxis

function b(operationStructure, fileUri) {
  var s = document.createElement("script");
  s.type = "application/javascript";
  // Se definen los atributos para que puedan ser usados después
  s.text =
    "var operationStructure = " +
    JSON.stringify(operationStructure) +
    ";var fileUri = '" +
    fileUri +
    "';";
  document.body.appendChild(s);

  // Devuelve 'http://localhost:8081/ideas-dockerfile-language/language/operation/$opId/javascript'
  var s1 = document.createElement("script");
  s1.type = "application/javascript";
  s1.src =
    ModeManager.getBaseUri(
      ModeManager.calculateModelIdFromExt(
        ModeManager.calculateExtFromFileUri(fileUri)
      )
    ) +
    "/language/operation/" +
    operationStructure.id +
    "/javascript";
  document.body.appendChild(s1);
}
