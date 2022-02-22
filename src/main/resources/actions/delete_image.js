async function del() {
  try {
    async function getSelectForm(data) {
      var res =
        "<fieldset>\
      <legend>Please select one of the following</legend>$content</fieldset>";
      var option =
        '<input name="imageNames" type="checkbox" id="$value" value="$value"/> <label for="$value">$name</label></br>';
      var uri =
        ModeManager.getBaseUri(
          ModeManager.calculateModelIdFromExt(
            ModeManager.calculateExtFromFileUri(fileUri)
          )
        ) + DEPRECATED_EXEC_OP_URI.replace("$opId", "get_images_to_delete");

      let tmp_data = {
        ...data,
      };

      tmp_data.id = "get_images_to_delete";
      var result = await $.ajax({
        url: uri,
        type: "POST",
        data: tmp_data,
      });

      console.log("onSuccess");
      console.log(uri);
      var images = JSON.parse(result.data);
      var content = "";
      for (var image in images) {
        content += option
          .replaceAll("$value", image)
          .replaceAll("$name", image + " " + images[image]);
      }
      res = res.replace("$content", content);
      return res;
    }
    function sendRequest(operationUri, data) {
      RequestHelper.ajax(operationUri, {
        type: "POST",
        data: data,
        onSuccess: async function (result) {
          console.log("onSuccess");
          console.log(operationUri);
          OperationMetrics.stop();
        },
        onProblems: async function (result) {
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
      ) + DEPRECATED_EXEC_OP_URI.replace("$opId", operationId);

    OperationMetrics.play(operationId);
    var form = await getSelectForm(data);
    OperationMetrics.stop();

    showModal(
      "Delete an image",
      form,
      "Delete",
      function () {
        var selected = $("[name='imageNames']");
        var res = "";
        for (var i = 0; i < selected.length; i++) {
          if (selected[i].checked) {
            res += selected[i].value + " ";
          }
        }
        data.imageName = res
        OperationMetrics.play(operationId);
        sendRequest(operationUri, data)
        closeModal();
      },
      closeModal,
      ""
    );
  } catch (error) {
    console.error(error);
  }
}
del();
