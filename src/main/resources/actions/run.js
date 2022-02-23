async function del() {
  try {
    function parseImagesOutput(htmlMessage) {
      var result = {};
      var rows = htmlMessage.split("\n");
      for (var row in rows) {
        if (!rows[row].includes("<")) {
          row = rows[row].split(" ");
          if (row[0] !== "") result[row[0]] = row[row.length - 1];
        }
      }
      return result;
    }
    async function getSelectForm(data) {
      var res =
        "<fieldset>\
      <legend>Please select one of the following</legend>$content</fieldset>";
      var option =
        '<input name="imageNames" type="radio" id="$value" value="$value"/> <label for="$value">$name</label></br>';
      var uri =
        ModeManager.getBaseUri(
          ModeManager.calculateModelIdFromExt(
            ModeManager.calculateExtFromFileUri(fileUri)
          )
        ) + DEPRECATED_EXEC_OP_URI.replace("$opId", "show_images");

      let tmp_data = {
        ...data,
      };

      tmp_data.id = "show_images";
      var result = await $.ajax({
        url: uri,
        type: "POST",
        data: tmp_data,
      });

      var images = parseImagesOutput(result.htmlMessage);

      var content = "";
      for (var image in images) {
        content += option
          .replaceAll("$value", image)
          .replaceAll("$name", image + " " + images[image]);
      }
      res = res.replace("$content", content);
      return res;
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
      "Run a container",
      form,
      "Run",
      function () {
        var selected = $("[name='imageNames']");
        var res = "";
        for (var i = 0; i < selected.length; i++) {
          if (selected[i].checked) {
            res += selected[i].value + " ";
          }
        }
        data.imageName = res;
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
}
del();
