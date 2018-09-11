<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<%@include file="./WEB-INF/includes/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>RevEngE</title>

</head>

<body>
<%@include file="./WEB-INF/includes/mainPane.jsp" %>
<table id="inner_content">
	<tr>
    	<td width="25%">
        <table class="inner_content_table">
        <%
        	ArrayList allStudents = myConnector.getAdminStudents((String)myUser.getAttribute("email"));
       		ArrayList courseNames = new ArrayList();
       		HashMap courseStudents = new HashMap();
        	for(int x=0; x<allStudents.size(); x++)
	        {
	        	DBObj curObj = (DBObj)allStudents.get(x);
	        	String course = (String)curObj.getAttribute("course");
	        	if(course.equals(""))
	        	{
	        		continue;
	        	}
	        	
	        	if(!courseNames.contains(course))
	        	{
	        		courseNames.add(course);
	        		courseStudents.put(course, new ArrayList());
	        	}
	        	ArrayList studentList = (ArrayList)courseStudents.get(course);
	        	studentList.add(curObj);
	        }
        	
        	String theSubject = request.getParameter("subject");
        	String theMessage = request.getParameter("message");
        	
        	ArrayList selectedCourses = new ArrayList();
        	Map requestParams = request.getParameterMap();
        	String courseString = "";
        	boolean first = true;
        	for(int x=0; x<courseNames.size(); x++)
        	{
        		String curCourse = (String)courseNames.get(x);
        		if(requestParams.containsKey(curCourse))
        		{
        			selectedCourses.add(curCourse);
        			if(first)
        			{
        				courseString += " " + curCourse;
        				first = false;
        			}
        			else
        			{
        				courseString += ", " + curCourse;
        			}
        		}
        	}
        	
        	for(int x=0; x<selectedCourses.size(); x++)
        	{
        		String curCourse = (String)selectedCourses.get(x);
        		ArrayList studentList = (ArrayList)courseStudents.get(curCourse);
        		for(int y=0; y<studentList.size(); y++)
        		{
        			DBObj curStudent = (DBObj)studentList.get(y);
        			String curEmail = (String)curStudent.getAttribute("email");
        			System.out.println(curEmail);
        			mySender.sendEmail(curEmail, theSubject, theMessage + "\n" + "You have received this message as part of your course " + curCourse + " at revenge.cs.arizona.edu.");
        		}
        	}
        	
        	mySender.sendEmail((String)myUser.getAttribute("email"), theSubject, theMessage + "\n" + "You have received this announcement because you made it at revenge.cs.arizona.edu.");
        %>
        <tr>
        <td>
        </td>
        </tr>
        </table>
        </td>
        <td width="50%">
        <table class="inner_content_table">
        <tr>
        <td>
        <table class="news_table">
        <tr class="title_general">
        <td align="center">
        Announcement
        </td>
    	</tr>
        <tr>
        <td>
        <table class="news_item_table" width="100%">
        <tr>
        <td colspan="2">
        You sent the following announcement to:<%=courseString %>
        </td>
        </tr>
        <tr>
        <td colspan="2">
        &nbsp;
        </td>
        </tr>
        <tr>
        <td colspan="2">
        Subject: <%=theSubject %>
        </td>
        </tr>
        <tr>
        <td colspan="2">
        <%=theMessage %>
        </td>
        </tr>
        </table>
        </td>
        </tr>
        </table>
        </td>
        </tr>
        </table>
        </td>
        <td width="25%">
        
        </td>
    </tr>
</table>
<%@include file="./WEB-INF/includes/footer.jsp" %>
</body>

</html>