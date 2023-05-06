<html>
<body>
    <form action="webapi/run_import_tool" method="post" enctype="multipart/form-data">
      Mode
      <select name="mode">
        <option value="lint">lint</option>
        <option value="genmcf">genmcf</option>
      </select>
      <br/><br/>
      Flags:<br/>
      <input type="checkbox" name="existence-checks" value="1" checked="checked"> existence-checks
      <br/>
      <input type="checkbox" name="existence-checks-place" value="1"> existence-checks-place
      <br/>
      <input type="checkbox" name="stat-checks" value="1" checked="checked"> stat-checks
      <br/>
      <input type="checkbox" name="summary-report" value="1" checked="checked"> summary-report
      <br/>
      <input type="checkbox" name="allow-non-numeric-obs-values" value="1"> allow-non-numeric-obs-values
      <br/>
      <input type="checkbox" name="check-measurement-result" value="1"> check-measurement-result
      <br/>
      <input type="checkbox" name="verbose" value="1"> verbose
      <br/>
      sample-places: <input type="text" name="sample-places" />
      <br/>
      Resolution:
      <select name="resolution">
        <option value="LOCAL">LOCAL</option>
        <option value="NONE">NONE</option>
        <option value="FULL">FULL</option>
      </select>
      <br/><br/>
      List of Input Files: <input type="file" name="input-files" multiple/>
      <br/>
      <br/><br/>
      <input type="submit" value="Submit" />
    </form>
</body>
</html>
