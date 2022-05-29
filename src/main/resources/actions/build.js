try {
  function getNameForm() {
    return "<input id='name'/>\
    ";
  }
  operationId = operationStructure.id;
  var data = {};
  data.fileUri = fileUri;
  data.content = EditorManager.getEditorContentByUri(fileUri);
  data.id = operationId;
  data.username = principalUser;

  // Devuelve 'http://localhost:8081/ideas-dockerfile-language/language/operation/$opId/execute'
  operationUri =
    ModeManager.getBaseUri(
      ModeManager.calculateModelIdFromExt(
        ModeManager.calculateExtFromFileUri(fileUri)
      )
    ) + DEPRECATED_EXEC_OP_URI.replace("$opId", operationId);

  showModal(
    "Introduce an image's name",
    getNameForm(),
    "Build",
    function () {
      imageName = $("#name").val();
      data.imageName = imageName;
      OperationMetrics.play(operationId);
      sendRequest(operationUri, data);
      closeModal();
    },
    closeModal,
    ""
  );
} catch (error) {
  console.error(error);
}
