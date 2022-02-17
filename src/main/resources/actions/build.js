// En language manifest, la operación build contiene esta función después de pasarla por https://jscompress.com/
// Luego se sustituyen las comillas dobles por simples para evitar problemas de sintaxis

function build(operationStructure, fileUri) {
  try {
    function getNameForm() {
      return "<input id='name'/>\
    ";
    }
    function sendCommand(operationUri, data) {
      RequestHelper.ajax(operationUri, {
        type: "POST",
        data: data,
        onSuccess: function (result) {
          console.log("onSuccess");
          console.log(operationUri);
        },
        onProblems: function (result) {
          console.log("onProblems");
        },
      });
    }
    function closeModal() {
      $("#appGenericModal").attr("style", "display: none;");
      $("#appGenericModal").attr("class", "modal");
      $("#appGenericModal").attr("aria-hidden", "true");
      $(".modal-backdrop").remove();
    }
    operationId = operationStructure.id;
    var data = {};
    data.fileUri = fileUri;
    data.content = EditorManager.getEditorContentByUri(fileUri);
    data.id = operationId;
    data.auxArg0 = principalUser;

    operationUri =
      ModeManager.getBaseUri(
        ModeManager.calculateModelIdFromExt(
          ModeManager.calculateExtFromFileUri(fileUri)
        )
      ) + DEPRECATED_EXEC_OP_URI.replace("$operationId", operationId);

    if (operationId === "build") {
      showModal(
        "Introduce nombre",
        getNameForm(),
        "Construir",
        function () {
          imageName = $("#name").val();
          data.imageName = imageName;
          sendCommand(operationUri, data);
          closeModal();
        },
        closeModal,
        ""
      );
    }
  } catch (error) {
    console.error(error);
  }
}
