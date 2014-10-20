<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro tagRenderer tag>
	<span class="label label-info"><#nested/></span>
</#macro>

<#macro fileModelRenderer fileModel>
  <#if isLiftAndShift(fileModel)>
    <#assign sourceReportModel = fileModelToSourceReport(fileModel)!>
    <#if sourceReportModel.reportFilename??>
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
	  </tr>
    </#if>
  </#if>
</#macro>

<#macro projectModelRenderer projectModel>
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">${projectModel.rootFileModel.prettyPath}</h3>
        </div>
        <table class="table table-striped table-bordered">
          <tr>
            <th>Name</th><th>Technology</th>
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

    <div class="container theme-showcase" role="main">



    <@projectModelRenderer reportModel.projectModel />
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>
