### java.lang.IllegalArgumentException: Illegal character in opaque part at index 2: <WORKSPACE>\sudoku\src\main\scala\it\unibo\pcd\akka\cluster\advanced\SudokuGUI.scala

Uri: <WORKSPACE>\sudoku\src\main\scala\it\unibo\pcd\akka\cluster\advanced\SudokuGUI.scala


#### Error stacktrace:

```
java.base/java.net.URI.create(URI.java:932)
	scala.meta.internal.mtags.MtagsEnrichments$XtensionStringMtags.toAbsolutePath(MtagsEnrichments.scala:187)
	scala.meta.internal.metals.MetalsEnrichments$XtensionString.toAbsolutePath(MetalsEnrichments.scala:741)
	scala.meta.internal.metals.MetalsEnrichments$XtensionString.toAbsolutePath(MetalsEnrichments.scala:738)
	scala.meta.internal.metals.MetalsEnrichments$XtensionString.toAbsolutePathSafe(MetalsEnrichments.scala:724)
	scala.meta.internal.metals.MetalsLspService.$anonfun$reports$2(MetalsLspService.scala:187)
	scala.Option.flatMap(Option.scala:283)
	scala.meta.internal.metals.MetalsLspService.$anonfun$reports$1(MetalsLspService.scala:185)
	scala.meta.internal.metals.StdReporter.reportPath(ReportContext.scala:163)
	scala.meta.internal.metals.StdReporter.create(ReportContext.scala:131)
	scala.meta.internal.metals.ReferenceProvider.$anonfun$references$2(ReferenceProvider.scala:176)
	scala.collection.immutable.List.map(List.scala:247)
	scala.meta.internal.metals.ReferenceProvider.references(ReferenceProvider.scala:151)
	scala.meta.internal.metals.MetalsLspService.referencesResult(MetalsLspService.scala:1635)
	scala.meta.internal.metals.MetalsLspService.$anonfun$references$1(MetalsLspService.scala:1577)
	scala.meta.internal.metals.CancelTokens$.$anonfun$apply$2(CancelTokens.scala:26)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:687)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:467)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

java.lang.IllegalArgumentException: Illegal character in opaque part at index 2: <WORKSPACE>\sudoku\src\main\scala\it\unibo\pcd\akka\cluster\advanced\SudokuGUI.scala