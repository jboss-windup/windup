<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro tagRenderer tag>
	<span class="label label-info"><#nested/></span>
</#macro>

<#macro fileModelRenderer fileModel>
  <#assign sourceReportModel = fileModelToSourceReport(fileModel)!>
  <#if !isLiftAndShift(fileModel) && sourceReportModel.reportFilename??>
	<tr>
	  <td>
	     <a href="${sourceReportModel.reportFilename}">
	       ${getPrettyPathForFile(fileModel)}
	     </a>
	  </td>
		<td>
			<#list getTechnologyTagsForFile(fileModel).iterator() as tag>
		    <@tagRenderer tag>${tag.name}</@tagRenderer>
		  </#list>
		</td>
		<td>
		  <#if sourceReportModel.sourceFileModel.inlineHints.iterator()?has_content>
  		  <b>Warnings: ${sourceReportModel.sourceFileModel.inlineHintCount} items</b>
          <ul class='notifications'>
            <#list sourceReportModel.sourceFileModel.inlineHints.iterator() as hintLine>
              <#if hintLine.hint?has_content>
                <li class='warning'>${hintLine.hint}</li>
              </#if>
            </#list>
          </ul>
      </#if>
		</td>
		<td>
		  <#assign fileEffort = getMigrationEffortPointsForFile(sourceReportModel.sourceFileModel)>
	    ${fileEffort}
		</td>
	</tr>
	</#if>
</#macro>

<#macro projectModelRenderer projectModel>
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">${projectModel.rootFileModel.prettyPath}</h3>
            
            <div class='col-md-3 text-right totalSummary'>
                <div class='totalLoe'>
                    ${getMigrationEffortPoints(projectModel, false)}
                </div>
                <div class='totalDesc'>Story Points</div>
            </div>
            
            <div class='col-md-6 pull-right windupPieGraph archiveGraphContainer'>
                <div id="project_${projectModel.asVertex().getId()?string("0")}_pie" class='windupPieGraph'></div>
            </div>
            
        </div>
        <table class="table table-striped table-bordered">
          <tr>
            <th>Name</th><th>Technology</th><th>Issues</th><th>Estimated Story Points</th>
          </tr>
          <#list sortFilesByPathAscending(projectModel.fileModelsNoDirectories) as fileModel>
             <@fileModelRenderer fileModel/>
          </#list>
        </table>
    </div>
  <#list sortProjectsByPathAscending(projectModel.childProjects) as childProject>
    <@projectModelRenderer childProject/>
  </#list>
</#macro>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Application Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
  </head>
  <body role="document">
    
    <!-- Fixed navbar -->
    <div class="navbar-fixed-top windup-bar" role="navigation">
      <div class="container theme-showcase" role="main">
        <img src="resources/img/windup-logo.png" class="logo"/>
      </div>
    </div>

    <div class="container" role="main">
    <div class="row">
      <div class="page-header page-header-no-border">
        <h1>Application Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.projectModel.name}</small></h1>
         <div class="navbar navbar-default">
          <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
        </div>
        <div class="navbar-collapse collapse navbar-responsive-collapse">
	  <ol class="breadcrumb top-menu">
		<li><a href="../index.html">All Applications</a></li>
			<#include "include/breadcrumbs.ftl">
	  </ol> 
          
        </div><!-- /.nav-collapse -->
        <div class="navbar-collapse collapse navbar-responsive-collapse">
          <ul class="nav navbar-nav">
            <#include "include/navbar.ftl">
          </ul>
        </div><!-- /.nav-collapse -->
	
      </div>
    </div>
</div>

  <div class='container mainGraphContainer'>
    <div class='col-md-3 text-right totalSummary'>
      <div class='totalLoe'>
        ${getMigrationEffortPoints(reportModel.projectModel, true)}
      </div>
      <div class='totalDesc'>Story Points</div>
    </div>
    <div class='col-md-6 pull-right windupPieGraph'>
      <div id='application_pie' class='windupPieGraph'>
      </div>
    </div>
  </div>

    <div class="container theme-showcase" role="main">



	     <@projectModelRenderer reportModel.projectModel />
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    
    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    
    <script src="resources/js/bootstrap.min.js"></script>

    <@render_pie project=reportModel.projectModel recursive=true elementID="application_pie"/>
    
    
    <#macro projectPieRenderer projectModel>
      <@render_pie project=projectModel recursive=false elementID="project_${projectModel.asVertex().getId()?string(\"0\")}_pie"/>
    
      <#list projectModel.childProjects.iterator() as childProject>
        <@projectPieRenderer childProject />
      </#list>
    </#macro>
    
    <@projectPieRenderer reportModel.projectModel />

  </body>
</html>
