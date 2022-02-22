try {
  function sendRequest(operationUri, data) {
    RequestHelper.ajax(operationUri, {
      type: "POST",
      data: data,
      onSuccess: async function (result) {
        console.log("onSuccess");
        console.log(operationUri);
        await result;
        OperationMetrics.stop();
      },
      onProblems: async function (result) {
        console.log("onProblems");
        await result;
        OperationMetrics.stop();
      },
    });
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
    ) + DEPRECATED_EXEC_OP_URI.replace("$operationId", operationId);

  OperationMetrics.play(operationId);
  sendRequest(operationUri, data);
} catch (error) {
  console.error(error);
}
