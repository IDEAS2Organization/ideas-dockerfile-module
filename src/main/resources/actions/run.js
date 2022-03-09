async function del() {
  try {
    function parseImagesOutput(htmlMessage) {
      var result = {};
      var rows = htmlMessage.split("\n");
      for (var row in rows) {
        if (!rows[row].includes("<")) {
          rows[row] = rows[row].replace(/\s+/gi, " "); // Sustituye un grupo de espacios por un único espacio 'imagen      300mb   44' -> 'imagen 300mb 44'
          row = rows[row].split(" ");
          if (row[0] !== "")
            result[row[0] + ":" + row[1]] = row[row.length - 1];
        }
      }
      return result;
    }

    function parseContainersOutput(htmlMessage) {
      var result = {};
      var rows = htmlMessage.split("\n");
      for (var row in rows) {
        if (!rows[row].includes("<")) {
          row_str = rows[row].split(" ");
          if (row_str[0] !== "")
            result[row_str[row_str.length - 1]] = rows[row];
        }
      }
      return result;
    }

    async function getSelectForm(data) {
      // Parte del form para las imágenes (crear un nuevo contenedor)
      var res =
        "<fieldset>\
      <legend>New container from image</legend>$content</fieldset>";
      var option =
        '<input name="radio-button" title="imageNames" type="radio" id="$value" value="$value"/> <label for="$value">$name</label></br>';
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
      tmp_data.flags = "";
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

      // Parte del form para los contenedores parados
      var res2 =
        "<fieldset>\
      <legend>Restart a stopped container</legend>$content2</fieldset>";
      var option2 =
        '<input name="radio-button" title="containerNames" type="radio" id="$value" value="$value"/> <label for="$value">$name</label></br>';
      var uri2 =
        ModeManager.getBaseUri(
          ModeManager.calculateModelIdFromExt(
            ModeManager.calculateExtFromFileUri(fileUri)
          )
        ) + DEPRECATED_EXEC_OP_URI.replace("$opId", "show_containers");

      let tmp_data2 = {
        ...data,
      };

      tmp_data2.id = "show_containers";
      tmp_data2.flags = "-f status=exited";
      var result2 = await $.ajax({
        url: uri2,
        type: "POST",
        data: tmp_data2,
      });

      var containers = parseContainersOutput(result2.htmlMessage);
      console.log(result2.htmlMessage);

      var content2 = "";
      for (var container in containers) {
        content2 += option2
          .replaceAll("$value", container)
          .replaceAll("$name", container);
      }
      res2 = res2.replace("$content2", content2);
      return res + res2;
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
        var selected1 = $("[title='imageNames']");
        var selected2 = $("[title='containerNames']");
        var isNew = "F";
        var res = "";

        for (var i = 0; i < selected1.length; i++) {
          if (selected1[i].checked) {
            isNew = "Y";
            res += selected1[i].value;
          }
        }

        if (isNew == "F") {
          for (var i = 0; i < selected2.length; i++) {
            if (selected2[i].checked) {
              res += selected2[i].value;
            }
          }
        }

        data.name = res;
        data.isNew = isNew;
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
