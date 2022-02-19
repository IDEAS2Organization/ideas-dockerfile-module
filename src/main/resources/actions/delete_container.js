try {
    function getNameForm() {
      return "<input id='name'/>\
      ";
    }
    function sendRequest(operationUri, data) {
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
    data.id = operationId;
    data.username = principalUser;
  
    // Devuelve 'http://localhost:8081/ideas-dockerfile-language/language/operation/$opId/execute'
    operationUri =
      ModeManager.getBaseUri(
        ModeManager.calculateModelIdFromExt(
          ModeManager.calculateExtFromFileUri(fileUri)
        )
      ) + DEPRECATED_EXEC_OP_URI.replace("$operationId", operationId);
  
    showModal(
      "Introduce a container's name or id",
      getNameForm(),
      "Delete",
      function () {
        nameOrId = $("#name").val();
        data.nameOrId = nameOrId;
        sendRequest(operationUri, data);
        closeModal();
      },
      closeModal,
      ""
    );
  } catch (error) {
    console.error(error);
  }