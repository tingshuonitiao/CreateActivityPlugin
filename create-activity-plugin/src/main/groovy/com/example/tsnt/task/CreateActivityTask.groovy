package com.example.tsnt.task

import com.example.tsnt.extension.CreateActivityExtension
import com.example.tsnt.template.ActivityNodeTemplate
import com.example.tsnt.template.ActivityTemplate
import com.example.tsnt.template.XmlTemplate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult;

/**
 * @Author: zhangxiaozong
 * @Date: 2018-03-24 21:30
 * @Description: 创建新的Activity的Task
 */

class CreateActivityTask extends DefaultTask {

    // 清单文件的相对路径
    public static final String manifestRelativePath = "/src/main/AndroidManifest.xml"

    @TaskAction
    void createActivity() {
        def extension = project.extensions.findByType(CreateActivityExtension)
        def applicationId = extension.applicationId
        def activityName = extension.activityName
        def packageName = extension.packageName
        generateXml(activityName)
        generateClass(activityName, packageName, applicationId)
        // 使用这种方式xml格式会改变
        // addToManifestByParseDom(activityName, packageName)
        addToManifesByFileIo(activityName, packageName)
    }

    // 创建Activiy对应的xml文件
    void generateXml(String activityName) {
        def xmlPath = project.projectDir.toString() + "/src/main/res/layout/"
        def fileName = "activity_" + activityName.toLowerCase() + ".xml"
        def template = new XmlTemplate().template
        generateFile(xmlPath, fileName, template)
    }

    // 创建Activity对应的java文件
    void generateClass(String activityName, String packageName, String applicationId) {
        def packagePath = applicationId.replace(".", "/") + "/" + packageName
        def activityPath = project.projectDir.toString() + "/src/main/java/" + packagePath + "/"
        def fileName = activityName + "Activity" + ".java"
        def binding = [
                applicationId: applicationId,
                packageName  : packageName,
                activityName : activityName + "Activity",
                xmlName      : "activity_" + activityName.toLowerCase()
        ]
        def activityTemplate = new ActivityTemplate()
        def template = makeTemplate(activityTemplate.template, binding)
        generateFile(activityPath, fileName, template)
    }

    // 通过文件读写流的方式将新创建的Activity加入清单文件
    void addToManifesByFileIo(String activityName, String packageName) {
        FileReader reader
        FileWriter writer
        try {
            reader = new FileReader(project.projectDir.toString() + manifestRelativePath)
            StringBuilder sb = new StringBuilder()
            // 每一行的内容
            String line = ""
            while ((line = reader.readLine()) != null) {
                // 找到application节点的末尾
                if (line.contains("</application>")) {
                    // 在application节点最后插入新创建的activity节点
                    def binding = [
                            packageName : packageName,
                            activityName: activityName,
                    ]
                    def template = makeTemplate(new ActivityNodeTemplate().template, binding)
                    sb.append(template.toString() + "\n")
                }
                sb.append(line + "\n")
            }
            String content = sb.toString()
            // 删除最后多出的一行
            content = content.substring(0, content.length() - 1)
            writer = new FileWriter(project.projectDir.toString() + manifestRelativePath)
            writer.write(content)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                reader.close()
            }
            if (writer != null) {
                writer.close()
            }
        }
    }

    // 通过解析xml的方式将新创建的Activity加入清单文件
    void addToManifestByParseDom(String activityName, String packageName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // 根据清单文件得到Document对象
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            File file = new File(project.projectDir.toString() + manifestRelativePath);
            Document doc = documentBuilder.parse(file)
            // 创建新的activity节点
            Element element = doc.createElement("activity");
            Attr attr = doc.createAttribute("android:name");
            attr.setValue("." + packageName + "." + activityName + "Activity")
            element.setAttributeNode(attr)
            Element root = doc.getDocumentElement()
            // 遍历Document对象, 在application节点最后插入新创建的activity节点
            NodeList childNodes = root.getChildNodes()
            for (int i = 0; i < childNodes.getLength(); i++) {
                println childNodes.item(i).getNodeName()
                if (childNodes.item(i).getNodeName().equals("application")) {
                    childNodes.item(i).appendChild(element)
                }
            }
            // 将修改后的Document对象写入到xml
            TransformerFactory tFactory = TransformerFactory.newInstance()
            Transformer transformer = tFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            DOMSource source = new DOMSource(doc)
            StreamResult result = new StreamResult(file)
            transformer.transform(source, result)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    // 加载模板
    def makeTemplate(def template, def binding) {
        def engine = new groovy.text.GStringTemplateEngine()
        return engine.createTemplate(template).make(binding)
    }

    // 根据模板生成文件
    void generateFile(def path, def fileName, def template) {
        File dir = new File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        File file = new File(path + fileName)
        if (!file.exists()) {
            file.createNewFile()
        } else {
            return
        }
        FileOutputStream out = new FileOutputStream(file, false)
        out.write(template.toString().getBytes("utf-8"))
        out.close()
    }
}
