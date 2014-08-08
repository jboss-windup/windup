<!DOCTYPE html>
<html lang="en">

<#assign mainNavigationIndexModel = sourceReport.mainNavigationIndexModel>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Source Report for ${sourceReport.reportName}</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
	  <link rel='stylesheet' type='text/css' href='resources/libraries/snippet/jquery.snippet.min.css' />
	  <link rel='stylesheet' type='text/css' href='resources/css/windup-source.css' />
	  <link rel='stylesheet' type='text/css' href='resources/libraries/sausage/sausage.css' />
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
            <h1>
              Source Report
              <span class="slash">/</span>
              <small style="margin-left: 20px; font-weight: 100;">${sourceReport.sourceFileModel.prettyPath}</small>
            </h1>
            <div class="navbar navbar-default">
            <div class="navbar-header">
              <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </button>
            </div>
            <div class="navbar-collapse collapse navbar-responsive-collapse">
              <ul class="nav navbar-nav">
                <#include "include/navbar.ftl">
              </ul>
            </div><!-- /.nav-collapse -->
            </div>
          </div>
        </div>
    </div>



    <div class="container theme-showcase" role="main">

    <div class="windupHighLevel">
      <h3>Classification</h3>
      
      <ul>
      <#list sourceReport.sourceFileModel.classificationModels.iterator() as classificationLineItem>
        <li>
          ${classificationLineItem.ruleID}
          <h3>Links</h3>
          <ul>
            <#list classificationLineItem.linkDecorators.iterator() as linkDecorator>
              <li>
                <a href='${linkDecorator.link}'>${linkDecorator.description}</a>
              </li>
          </#list>
          </ul>
        </li>
      </#list>
      </ul>
    </div>

	<pre id='source'><#t><#rt>
		${sourceReport.sourceBody?html}<#t><#rt>
	</pre><#t><#rt>
	
    </div> <!-- /container -->

    <script src="resources/js/jquery-1.7.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    
	<script type='text/javascript' src='resources/libraries/jquery-ui/jquery.ui.widget.js'></script>
	<script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.min.js'></script>
	<script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.java-properties.js'></script>
	<script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.java-manifest.js'></script>
	<script type='text/javascript' src='resources/libraries/sausage/jquery.sausage.min.js'></script>

    
	    
	<script type='text/javascript'>
		$(document).ready(function(){
			$('pre').snippet('${sourceReport.sourceType}',{style:'ide-eclipse', showNum:true,boxFill:'#ffeeb9', box: '${sourceReport.sourceBlock}' });
	
	
		<#list sourceReport.sourceFileModel.blackListModels.iterator() as blackListLine>
			<#assign lineNumber = blackListLine.lineNumber>
			$("<div id='${lineNumber?c}-inlines' class='inline-source-hint-group'/>").appendTo('ol.snippet-num li:nth-child(${lineNumber?c})');
		</#list>
		
		<#list sourceReport.sourceFileModel.blackListModels.iterator() as blackListLine>
			<#assign lineNumber = blackListLine.lineNumber>
			
			<#compress>
			$("<a name='${blackListLine.hashCode()?c}'></a><#t>
				<div class='inline-source-comment green'><#t>
					<#if blackListLine.hint?has_content>
						<div class='inline-comment'><div class='inline-comment-heading'><strong class='notification ${blackListLine.effort}'>${blackListLine.hint?js_string}</strong></div><#t>
						</div><#t>
					</#if>
				</div><#t>
			").appendTo('#${lineNumber?c}-inlines');<#t>
			</#compress>

		</#list>


		
			$('code[data-code-syntax]').each(function(){
		         var codeSyntax = ($(this).data('code-syntax'));
		         if(codeSyntax) {
		            $(this).parent().snippet(codeSyntax,{style:'ide-eclipse', menu:false, showNum:false});
		         }
			});
			$(window).sausage({ page: 'li.box' });
		}); 
	</script>
    
  </body>
</html>