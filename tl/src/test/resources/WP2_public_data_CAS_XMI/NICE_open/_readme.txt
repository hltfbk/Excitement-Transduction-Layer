The folder contains transformed XMI files for "NICE_open" data (src\test\resources\WP2_public_data\NICE_open).
  
( The transformation is really easy, and you can do it too, just by adding input and output dir to "ConvertWP2PublicData.java" file in src/test/java, under package tl.laputils. ) 
Note that about a dozen files couldn't be transformed due to mismatching texts, missing XML, ill formed data, etc. --- this is the same for previous open data. In total, this new data section has 249 XMI files. 

Lili: I re-created the data after re-annotation, since fragment graphs were edited during this process. 
I created the xmi-s for each cluster separately and then copied them to from "test/allClusters", 
"train/allClusters" and "all" directories. I am not sure the contents of these 3 directories are fully valid,
since there were files with the same name generated within different clusters, 
and when merging I only kept one version of such files. 