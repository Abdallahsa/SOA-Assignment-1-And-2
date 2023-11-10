package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.ui.Model;
@Controller
@RequestMapping("/students")
public class StudentController {

    private static final String XML_FILE_PATH = "D:\\7th semester\\SOA\\code\\assignment 1\\src\\main\\java\\com\\example\\demo\\test.xml";

    @GetMapping("/allStudents")
    public String getAllStudents(Model model) {
        List<StudentRequest> result = new ArrayList<>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new File(XML_FILE_PATH));

            NodeList studentNodes = doc.getElementsByTagName("Student");

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);

                StudentRequest studentResponse = new StudentRequest();
                studentResponse.setId(studentElement.getAttribute("ID"));
                studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
                studentResponse.setLastName(getElementValue(studentElement, "LastName"));
                studentResponse.setGender(getElementValue(studentElement, "Gender"));
                studentResponse.setGpa(Double.parseDouble(getElementValue(studentElement, "GPA")));
                studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
                studentResponse.setAddress(getElementValue(studentElement, "Address"));

                result.add(studentResponse);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("students", result);
        return "viewAllStudents";
    }

@PostMapping("/saveStudents")
public String saveStudents(@RequestBody List<StudentRequest> studentRequests) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
    try {
        DocumentBuilder builder = dbf.newDocumentBuilder();

        // Load existing XML document if it exists
        Document doc;
        File xmlFile = new File(XML_FILE_PATH);
        if (xmlFile.exists()) {
            doc = builder.parse(xmlFile);
        } else {
            doc = builder.newDocument();
            Element root = doc.createElement("University");
            doc.appendChild(root);
        }

        Element root = doc.getDocumentElement();

        for (StudentRequest studentRequest : studentRequests) {
            // Check if student with given ID already exists
            if (isStudentAlreadySaved(doc, studentRequest.getId())) {
                    System.out.println("Student with ID " + studentRequest.getId() + " is already saved.");
                continue; // Skip to the next student
            }

            Element student = doc.createElement("Student");
            student.setAttribute("ID", studentRequest.getId());

            Element firstName = doc.createElement("FirstName");
            Text firstNameVal = doc.createTextNode(studentRequest.getFirstName());
            firstName.appendChild(firstNameVal);

            Element lastName = doc.createElement("LastName");
            Text lastNameVal = doc.createTextNode(studentRequest.getLastName());
            lastName.appendChild(lastNameVal);

            Element gender = doc.createElement("Gender");
            Text genderVal = doc.createTextNode(studentRequest.getGender());
            gender.appendChild(genderVal);

            Element gpa = doc.createElement("GPA");
            Text gpaVal = doc.createTextNode(String.valueOf(studentRequest.getGpa()));
            gpa.appendChild(gpaVal);
            
            Element level = doc.createElement("Level");
            Text levelVal = doc.createTextNode(String.valueOf(studentRequest.getLevel()));
            level.appendChild(levelVal);

            Element address = doc.createElement("Address");
            Text addressVal = doc.createTextNode(studentRequest.getAddress());
            address.appendChild(addressVal);

            student.appendChild(firstName);
            student.appendChild(lastName);
            student.appendChild(gender);
            student.appendChild(gpa);
            student.appendChild(level);
            student.appendChild(address);

            root.appendChild(student);
        }

        DOMSource source = new DOMSource(doc);

        Result result = new StreamResult(xmlFile);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);

        System.out.println("OK" + XML_FILE_PATH);

    } catch (Exception e) {
        throw new RuntimeException(e);
    }

    return "Students saved successfully.";
}

    @GetMapping("/searchByGPA")
    public String searchByGPA(@RequestParam double gpa, Model model) {
        List<StudentRequest> result = new ArrayList<>();

        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                System.out.println("No students have been saved yet.");
                model.addAttribute("students", result);

                return "searchByGpaResult"; // Return an empty list indicating no students found
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList studentNodes = doc.getElementsByTagName("Student");

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);
                String studentId = studentElement.getAttribute("ID");
                double studentGPA = Double.parseDouble(getElementValue(studentElement, "GPA"));

                if (studentGPA == gpa) {
                    StudentRequest studentResponse = new StudentRequest();
                    studentResponse.setId(studentId);
                    studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
                    studentResponse.setLastName(getElementValue(studentElement, "LastName"));
                    studentResponse.setGender(getElementValue(studentElement, "Gender"));
                    studentResponse.setGpa(studentGPA);
                    studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
                    studentResponse.setAddress(getElementValue(studentElement, "Address"));

                    result.add(studentResponse);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("students", result);

        return "searchByGpaResult";
    }


    @GetMapping("/searchByFirstName")
    public List<StudentRequest> searchByFirstName(@RequestParam String firstName) {
        List<StudentRequest> result = new ArrayList<>();

        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                System.out.println("No students have been saved yet.");
                return result; // Return an empty list indicating no students found
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList studentNodes = doc.getElementsByTagName("Student");

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);
                String studentId = studentElement.getAttribute("ID");
                String studentFirstName = getElementValue(studentElement, "FirstName");

                if (studentFirstName != null && studentFirstName.equals(firstName)) {
                    StudentRequest studentResponse = new StudentRequest();
                    studentResponse.setId(studentId);
                    studentResponse.setFirstName(studentFirstName);
                    studentResponse.setLastName(getElementValue(studentElement, "LastName"));
                    studentResponse.setGender(getElementValue(studentElement, "Gender"));
                    studentResponse.setGpa(Double.parseDouble(getElementValue(studentElement, "GPA")));
                    studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
                    studentResponse.setAddress(getElementValue(studentElement, "Address"));

                    result.add(studentResponse);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @DeleteMapping("/deleteById/{id}")
    public String deleteStudentById(@PathVariable String id) {
        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                System.out.println("No students have been saved yet.");
                return "No students have been saved yet.";
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList studentNodes = doc.getElementsByTagName("Student");

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);

                if (studentElement.getAttribute("ID").equals(id)) {
                    // Remove the matching student element
                    Node parentNode = studentElement.getParentNode();
                    parentNode.removeChild(studentElement);

                    // Save the updated XML without introducing extra spaces
                    saveUpdatedXml(doc);

                    return "Student with ID " + id + " deleted successfully.";
                }
            }

            return "Student with ID " + id + " not found.";

        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging in a real application
            return "An error occurred during the deletion process.";
        }
    }


    private void saveUpdatedXml(Document doc) throws TransformerException {
        try {
            // Use a StringWriter to prevent extra spaces when saving
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            // Use the transformer to write the XML content to the StringWriter
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), result);

            // Update the file with the content from the StringWriter
            try (FileWriter fileWriter = new FileWriter(new File(XML_FILE_PATH))) {
                fileWriter.write(sw.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String getElementValue(Element parentElement, String elementName) {
        NodeList nodeList = parentElement.getElementsByTagName(elementName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private boolean isStudentAlreadySaved(Document doc, String studentId) {
        Element root = doc.getDocumentElement();
        Element[] students = getChildElements(root, "Student");

        for (Element student : students) {
            if (studentId.equals(student.getAttribute("ID"))) {
                return true;
            }
        }

        return false;
    }

    // Helper method to get child elements by tag name
    private Element[] getChildElements(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        List<Element> elements = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) node);
            }
        }

        return elements.toArray(new Element[0]);
    }

}
