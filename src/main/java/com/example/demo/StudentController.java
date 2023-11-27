package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private static final String XML_FILE_PATH = "src\\main\\java\\com\\example\\demo\\test.xml";

    @GetMapping("/allStudents")
    public List<StudentRequest> getAllStudents() {
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
        return result;
    }

@PostMapping("/saveStudents")
public String saveStudents(@RequestBody List<StudentRequest> studentRequests) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
    List<String> duplicateIds = new ArrayList<>();
    List<String> invalidNames = new ArrayList<>();
    List<String> invalidAddresses = new ArrayList<>();
    List<String> invalidLevels = new ArrayList<>();
    List<String> invalidGpas = new ArrayList<>();
    List<String> invalidGenders = new ArrayList<>();
    List<String> invalidIds = new ArrayList<>();

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
            if (studentRequest.getId() == null || studentRequest.getId().isEmpty()) {
                System.out.println("Invalid ID for a student. Skipping.");
                invalidIds.add(studentRequest.getId());
                continue; // Skip to the next student
            }

            // Check if student with given ID already exists
            if (isStudentAlreadySaved(doc, studentRequest.getId())) {
                    System.out.println("Student with ID " + studentRequest.getId() + " is already saved.");
                duplicateIds.add(studentRequest.getId());
                continue; // Skip to the next student
            }

            // Validate First Name
            if (!isNameAndAddressValid(studentRequest.getFirstName())) {
                System.out.println("Invalid First Name for student with ID " + studentRequest.getId());
                invalidNames.add(studentRequest.getId());
                continue; // Skip to the next student
            }

            // Validate Last Name
            if (!isNameAndAddressValid(studentRequest.getLastName())) {
                System.out.println("Invalid Last Name for student with ID " + studentRequest.getId());
                invalidNames.add(studentRequest.getId());
                continue; // Skip to the next student
            }

            // Validate Address
            if (!isNameAndAddressValid(studentRequest.getAddress())) {
                System.out.println("Invalid Address for student with ID " + studentRequest.getId());
                invalidAddresses.add(studentRequest.getId());
                continue; // Skip to the next student
            }

            //validate gpa
            String gpaString = String.valueOf(studentRequest.getGpa());
            if (gpaString == null || gpaString.isEmpty()) {
                System.out.println("Invalid GPA for student with ID " + studentRequest.getId());
                invalidGpas.add(studentRequest.getId());
                continue; // Skip to the next student
            }else if(!(studentRequest.getGpa()>=0 && studentRequest.getGpa()<=4)){
                System.out.printf("Invalid GPA for student with ID " + studentRequest.getId());
                invalidGpas.add(studentRequest.getId());
                continue; // Skip to the next student
            }


            //validate level
            if(studentRequest.getLevel() < 1 || studentRequest.getLevel() > 4)
            {
            	System.out.println("Invalid Level for student with ID " + studentRequest.getId());
                	invalidLevels.add(studentRequest.getId());
            	continue;
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

    if (duplicateIds.isEmpty() && invalidNames.isEmpty() && invalidAddresses.isEmpty() && invalidGenders.isEmpty() && invalidGpas.isEmpty() && invalidLevels.isEmpty() && invalidIds.isEmpty() ) {
        return "OK";
    } else {
        StringBuilder sb = new StringBuilder();
        if (!duplicateIds.isEmpty()) {
            sb.append("Duplicate IDs: ").append(duplicateIds).append("\n");
        }
        if (!invalidNames.isEmpty()) {
            sb.append("Invalid Names: ").append(invalidNames).append("\n");
        }
        if (!invalidAddresses.isEmpty()) {
            sb.append("Invalid Addresses: ").append(invalidAddresses).append("\n");
        }
        if(!invalidGenders.isEmpty()){
            sb.append("Invalid Gender: ").append(invalidGenders).append("\n");
        }
        if(!invalidGpas.isEmpty()){
            sb.append("Invalid GPA: ").append(invalidGpas).append("\n");
        }
        if(!invalidLevels.isEmpty()){
            sb.append("Invalid Level: ").append(invalidLevels).append("\n");
        }
        if(!invalidIds.isEmpty()){
            sb.append("Invalid ID: ").append(invalidIds).append("\n");
        }

        return sb.toString();
    }
}

    @GetMapping("/searchByGPA")
    public List<StudentRequest> searchByGPA(@RequestParam double gpa) {
        List<StudentRequest> result = new ArrayList<>();

        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                System.out.println("No students have been saved yet.");
              
                return result;
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

        return result;
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

    @PutMapping("/updateStudent/{id}")
    public String updateStudent(@PathVariable String id, @RequestBody StudentRequest updatedStudent) {
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
                    // Update the student attributes
                    updateStudentAttributes(studentElement, updatedStudent);

                    // Save the updated XML without introducing extra spaces
                    saveUpdatedXml(doc);

                    return "Student with ID " + id + " updated successfully.";
                }
            }

            return "Student with ID " + id + " not found.";

        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging in a real application
            return "An error occurred during the update process.";
        }
    }

    private void updateStudentAttributes(Element studentElement, StudentRequest updatedStudent) {
        // Update only the provided attributes
        if (updatedStudent.getFirstName() != null) {
            updateElementValue(studentElement, "FirstName", updatedStudent.getFirstName());
        }
        if (updatedStudent.getLastName() != null) {
            updateElementValue(studentElement, "LastName", updatedStudent.getLastName());
        }
        if (updatedStudent.getGender() != null) {
            updateElementValue(studentElement, "Gender", updatedStudent.getGender());
        }
        if (!String.valueOf(updatedStudent.getGpa()).isEmpty()  ) {
            if(updatedStudent.getGpa()>=0 && updatedStudent.getGpa()<=4){
                updateElementValue(studentElement, "GPA", String.valueOf(updatedStudent.getGpa()));
            }

        }
// Check if level is provided; if not, preserve the existing value
        if (!String.valueOf(updatedStudent.getLevel()).isEmpty()) {
            // Validate level and update if valid
            if (updatedStudent.getLevel() >= 1 && updatedStudent.getLevel() <= 4) {
                updateElementValue(studentElement, "Level", String.valueOf(updatedStudent.getLevel()));
            }
        }
        if (updatedStudent.getAddress() != null) {
            updateElementValue(studentElement, "Address", updatedStudent.getAddress());
        }

    }


    private void updateElementValue(Element parentElement, String elementName, String updatedValue) {
        NodeList nodeList = parentElement.getElementsByTagName(elementName);
        if (nodeList.getLength() > 0) {
            nodeList.item(0).setTextContent(updatedValue);
        } else {
            // If the element doesn't exist, create a new one
            Element newElement = parentElement.getOwnerDocument().createElement(elementName);
            Text textNode = parentElement.getOwnerDocument().createTextNode(updatedValue);
            newElement.appendChild(textNode);
            parentElement.appendChild(newElement);
        }
    }

    @GetMapping("/searchStudents")
    public List<StudentRequest> searchStudents(@RequestParam(required = false) String firstName,
                                                @RequestParam(required = false) String lastName,
                                                @RequestParam(required = false) String gender,
                                                @RequestParam(required = false) Double gpa,
                                                @RequestParam(required = false) Integer level,
                                                @RequestParam(required = false) String address) {
        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                System.out.println("No students have been saved yet.");
                return Collections.emptyList();
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList studentNodes = doc.getElementsByTagName("Student");

            List<StudentRequest> matchingStudents = new ArrayList<>();

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);
                System.out.println(studentElement.getAttribute("ID"));

                // Check if the student matches the search criteria
                if (matchesSearchCriteria(studentElement, firstName, lastName, gender, gpa, level, address)) {
                    // Create a StudentResponse object with the relevant details
                    StudentRequest studentResponse = createStudentResponse(studentElement);
                    matchingStudents.add(studentResponse);
                }
            }

            System.out.println("Number of found students: " + matchingStudents.size());

            return matchingStudents;

        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging in a real application
            return Collections.emptyList();
        }
    }

    private boolean matchesSearchCriteria(Element studentElement, String firstName, String lastName,
                                          String gender, Double gpa, Integer level, String address) {
        // Implement logic to check if the student matches the search criteria
        // You can customize this based on your specific requirements

        String studentFirstName = getElementValue(studentElement, "FirstName").trim();
        String studentLastName = getElementValue(studentElement, "LastName").trim();
        String studentGender = getElementValue(studentElement, "Gender").trim();
        String studentAddress = getElementValue(studentElement, "Address").trim();

        return (firstName == null || studentFirstName != null && studentFirstName.equalsIgnoreCase(firstName.trim())) &&
                (lastName == null || studentLastName.equalsIgnoreCase(lastName.trim())) &&
                (gender == null || studentGender.equalsIgnoreCase(gender.trim())) &&
                (gpa == null || gpa.equals(Double.parseDouble(getElementValue(studentElement, "GPA").trim()))) &&
                (level == null || level.equals(Integer.parseInt(getElementValue(studentElement, "Level").trim()))) &&
                (address == null || studentAddress.equalsIgnoreCase(address.trim()));
    }



    private StudentRequest createStudentResponse(Element studentElement) {
        // Create a StudentResponse object with the relevant details from the XML
        StudentRequest studentResponse = new StudentRequest();
        studentResponse.setId(studentElement.getAttribute("ID"));
        studentResponse.setFirstName(getElementValue(studentElement, "FirstName"));
        studentResponse.setLastName(getElementValue(studentElement, "LastName"));
        studentResponse.setGender(getElementValue(studentElement, "Gender"));
        studentResponse.setGpa(Double.parseDouble(getElementValue(studentElement, "GPA")));
        studentResponse.setLevel(Integer.parseInt(getElementValue(studentElement, "Level")));
        studentResponse.setAddress(getElementValue(studentElement, "Address"));
        return studentResponse;
    }

    @GetMapping("/sortStudents")
    public ResponseEntity<List<StudentRequest>> sortStudents(
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                System.out.println("No students have been saved yet.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            // Validate input parameters
            if (!isValidSortAttribute(sortBy) || !isValidSortOrder(sortOrder)) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList studentNodes = doc.getElementsByTagName("Student");

            List<StudentRequest> sortedStudents = new ArrayList<>();

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);
                StudentRequest studentResponse = createStudentResponse(studentElement);
                sortedStudents.add(studentResponse);
            }

            // Sort the list based on the specified attribute and order
            sortStudentList(sortedStudents, sortBy, sortOrder);

            return ResponseEntity.ok(sortedStudents);

        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging in a real application
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    private void sortStudentList(List<StudentRequest> students, String sortBy, String sortOrder) {
        students.sort((s1, s2) -> {
            Comparable value1 = getSortableAttributeValue(s1, sortBy);
            Comparable value2 = getSortableAttributeValue(s2, sortBy);

            if (sortBy.equalsIgnoreCase("id")) {
                // Convert ID to numbers for sorting
                value1 = Long.parseLong((String) value1);
                value2 = Long.parseLong((String) value2);
            }

            // Handle ascending or descending order
            int result = sortOrder.equalsIgnoreCase("asc") ? value1.compareTo(value2) : value2.compareTo(value1);

            // If the primary sort attributes are equal, use ID as a secondary sort criterion
            if (result == 0) {
                return s1.getId().compareTo(s2.getId());
            }

            return result;
        });
    }

    private Comparable getSortableAttributeValue(StudentRequest student, String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "id":
                return student.getId();
            case "firstname":
                return student.getFirstName();
            case "lastname":
                return student.getLastName();
            case "gender":
                return student.getGender();
            case "gpa":
                return student.getGpa();
            case "level":
                return student.getLevel();
            case "address":
                return student.getAddress();
            // Add more cases for other attributes if needed
            default:
                throw new IllegalArgumentException("Invalid attribute for sorting: " + sortBy);
        }
    }

    private boolean isValidSortAttribute(String attribute) {
        // Add more attributes if needed
        return List.of("id", "firstname", "lastname", "gender", "gpa", "level", "address").contains(attribute.toLowerCase());
    }

    private boolean isValidSortOrder(String order) {
        return order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc");
    }






    @GetMapping("/sortAndSaveStudents")
    public String sortAndSaveStudents(@RequestParam String sortBy, @RequestParam(defaultValue = "asc") String sortOrder) {
        try {
            File xmlFile = new File(XML_FILE_PATH);

            // Check if the file exists before attempting to parse it
            if (!xmlFile.exists()) {
                return "No students have been saved yet.";
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList studentNodes = doc.getElementsByTagName("Student");

            List<StudentRequest> sortedStudents = new ArrayList<>();

            for (int i = 0; i < studentNodes.getLength(); i++) {
                Element studentElement = (Element) studentNodes.item(i);
                StudentRequest studentResponse = createStudentResponse(studentElement);
                sortedStudents.add(studentResponse);
            }

            // Sort the list based on the specified attribute and order
            sortStudentList(sortedStudents, sortBy, sortOrder);

            // Save the sorted content back to the XML file
            saveSortedXml(sortedStudents, doc);

            return "File sorted and saved successfully.";

        } catch (Exception e) {
            e.printStackTrace(); // Add proper logging in a real application
            return "An error occurred while sorting and saving the file.";
        }
    }

    private void saveSortedXml(List<StudentRequest> sortedStudents, Document doc) throws TransformerException {
        try {
            // Use a StringWriter to prevent extra spaces when saving
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            // Use the transformer to write the XML content to the StringWriter
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Check if the root element exists; if not, create it
            Element root = doc.getDocumentElement();
            if (root == null) {
                root = doc.createElement("University");
                doc.appendChild(root);
            }

            // Clear existing content
            root.setTextContent("");
            sortedStudents.sort(Comparator.comparingInt(student -> Integer.parseInt(student.getId())));

            // Append each student element to the document
            for (StudentRequest student : sortedStudents) {
                Element studentElement = doc.createElement("Student");
                studentElement.setAttribute("ID", student.getId());

                Element firstName = doc.createElement("FirstName");
                firstName.setTextContent(student.getFirstName());
                studentElement.appendChild(firstName);

                Element lastName = doc.createElement("LastName");
                lastName.setTextContent(student.getLastName());
                studentElement.appendChild(lastName);

                Element gender = doc.createElement("Gender");
                gender.setTextContent(student.getGender());
                studentElement.appendChild(gender);

                Element gpa = doc.createElement("GPA");
                gpa.setTextContent(String.valueOf(student.getGpa()));
                studentElement.appendChild(gpa);

                Element level = doc.createElement("Level");
                level.setTextContent(String.valueOf(student.getLevel()));
                studentElement.appendChild(level);

                Element address = doc.createElement("Address");
                address.setTextContent(student.getAddress());
                studentElement.appendChild(address);

                // Append the student element to the root element
                root.appendChild(studentElement);
            }

            // Update the file with the content from the StringWriter
            transformer.transform(new DOMSource(doc), result);

            try (FileWriter fileWriter = new FileWriter(new File(XML_FILE_PATH))) {
                fileWriter.write(sw.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        System.out.println("NodeList Length: " + nodeList.item(0).getTextContent());
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

    private boolean isNameAndAddressValid(String nameOrAddress) {
        return nameOrAddress != null && !nameOrAddress.isEmpty() && nameOrAddress.matches("^[a-zA-Z]+$");
    }




}
