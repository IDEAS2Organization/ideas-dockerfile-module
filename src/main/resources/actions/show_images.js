try {
  function getSelectFlagsForm(data) {
    var res =
      "<fieldset>\
    <legend>Please select one of the following</legend>$content</fieldset>";
    var option =
      '<input name="flagsNames" type="checkbox" id="$value" value="$value"/> <label data-toggle="tooltip" title="$help" for="$value">$name</label></br>';

    const allFlags = {
      "--all": "Muestra todos los contenedores",
      "--digests": "Muestra el hash 'sha1' de la imagen",
      "--no-trunc": "No corta la salida",
      "--quiet": "Muestra únicamente los id",
    };

    var content = "";
    for (var i in allFlags) {
      // Crea un input type checkbox por flag posible
      content += option
        .replaceAll("$value", i)
        .replaceAll("$name", i)
        .replaceAll("$help", allFlags[i]);
    }
    res = res.replace("$content", content);
    return res;
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

  //OperationMetrics.play(operationId);
  //sendRequest(operationUri, data);

  var form = getSelectFlagsForm();

  showModal(
    "Show images",
    form,
    "Show",
    function () {
      var selected = $("[name='flagsNames']");
      var res = "";
      for (var i = 0; i < selected.length; i++) {
        if (selected[i].checked) {
          res += selected[i].value + " ";
        }
      }
      data.flags = res;
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
