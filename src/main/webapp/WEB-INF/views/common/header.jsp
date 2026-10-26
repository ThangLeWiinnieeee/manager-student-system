<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="currentPath"
       value="${requestScope['jakarta.servlet.forward.request_uri']}"/>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>
        <c:out value="${empty pageTitle ? 'Quản lý đề tài sinh viên' : pageTitle}"/>
        | Khoa CNTT
    </title>

    <link rel="stylesheet"
          href="<c:url value='/css/app.css'/>">
</head>

<body>

    <%-- ========================================= --%>
    <%-- SKIP LINK                                 --%>
    <%-- ========================================= --%>

    <a class="skip-link" href="#main-content">
        Đến nội dung chính
    </a>


    <%-- ========================================= --%>
    <%-- SIDEBAR                                   --%>
    <%-- ========================================= --%>

    <aside class="sidebar">

        <%-- LOGO VA TEN HE THONG --%>

        <a class="brand" href="<c:url value='/'/>">

            <span class="brand-mark" aria-hidden="true">

                <svg class="nav-icon"
                     viewBox="0 0 24 24"
                     aria-hidden="true">

                    <path d="M3 7V4h6l3 3h9v13H3z"/>
                    <path d="M3 10h18"/>

                </svg>

            </span>

            <span>
                Quản lý đề tài
                <small>Khoa Công nghệ thông tin</small>
            </span>

        </a>


        <p class="nav-label">
            Không gian làm việc
        </p>


        <%-- ========================================= --%>
        <%-- MENU DIEU HUONG CHINH                     --%>
        <%-- ========================================= --%>

        <nav aria-label="Điều hướng chính">


            <%-- ===================================== --%>
            <%-- 1. TONG QUAN - MOI VAI TRO            --%>
            <%-- ===================================== --%>

            <a class="nav-link"
               href="<c:url value='/'/>"
               aria-current="${currentPath == pageContext.request.contextPath.concat('/') ? 'page' : 'false'}">

                <svg class="nav-icon"
                     viewBox="0 0 24 24"
                     aria-hidden="true">

                    <path d="m3 10 9-7 9 7v10H3zM9 20v-7h6v7"/>

                </svg>

                Tổng quan
            </a>


            <%-- ===================================== --%>
            <%-- 2. DE TAI - TV2                       --%>
            <%-- ===================================== --%>

            <a class="nav-link"
               href="<c:url value='/topics'/>"
               aria-current="${fn:contains(currentPath, '/topics') ? 'page' : 'false'}">

                <svg class="nav-icon"
                     viewBox="0 0 24 24"
                     aria-hidden="true">

                    <path d="M3 7V4h6l3 3h9v13H3z"/>
                    <path d="M3 10h18"/>

                </svg>

                Đề tài
            </a>


            <%-- ===================================== --%>
            <%-- THONG BAO - MOI VAI TRO               --%>
            <%-- ===================================== --%>

            <a class="nav-link"
               href="<c:url value='/notifications'/>"
               aria-current="${fn:contains(currentPath, '/notifications') ? 'page' : 'false'}">

                <svg class="nav-icon"
                     viewBox="0 0 24 24"
                     aria-hidden="true">

                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                    <path d="M13.73 21a2 2 0 0 1-3.46 0"/>

                </svg>

                Thông báo
            </a>


            <%-- ========================================= --%>
            <%-- QUAN LY TAI KHOAN CHI DANH CHO ADMIN    --%>
            <%-- ========================================= --%>

            <c:if test="${pageContext.request.isUserInRole('ADMIN')}">


                <%-- ================================= --%>
                <%-- 3. QUAN LY TAI KHOAN               --%>
                <%-- ================================= --%>

                <a class="nav-link"
                   href="<c:url value='/admin/users'/>"
                   aria-current="${fn:contains(currentPath, '/admin/users') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <circle cx="9" cy="8" r="3"/>

                        <path d="M3 21v-3a6 6 0 0 1 12 0v3
                                 M16 5a3 3 0 0 1 0 6
                                 m2 3a5 5 0 0 1 3 5v2"/>

                    </svg>

                    Tài khoản
                </a>
            </c:if>
            <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN')}">

                <%-- ================================= --%>
                <%-- 4. QUAN LY BO MON                 --%>
                <%-- ================================= --%>

                <a class="nav-link"
                   href="<c:url value='/admin/departments'/>"
                   aria-current="${fn:contains(currentPath, '/admin/departments') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <path d="M4 21V5l8-3 8 3v16H4
                                 M9 21v-5h6v5
                                 M8 8h1m6 0h1
                                 M8 12h1m6 0h1"/>

                    </svg>

                    Bộ môn
                </a>


                <%-- ================================= --%>
                <%-- 5. DOT DANG KY - TV2              --%>
                <%-- ================================= --%>

                <a class="nav-link"
                   href="<c:url value='/admin/registration-periods'/>"
                   aria-current="${fn:contains(currentPath, '/admin/registration-periods') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <rect x="3"
                              y="5"
                              width="18"
                              height="16"
                              rx="2"/>

                        <path d="M8 3v4m8-4v4M3 11h18"/>

                    </svg>

                    Đợt đăng ký
                </a>


                <%-- ================================= --%>
                <%-- 6. NHOM SINH VIEN - TV3            --%>
                <%-- ================================= --%>

                <c:if test="${pageContext.request.isUserInRole('ADMIN')}">
                <a class="nav-link"
                   href="<c:url value='/admin/student-groups'/>"
                   aria-current="${fn:contains(currentPath, '/admin/student-groups') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <circle cx="9" cy="8" r="3"/>

                        <path d="M3 21v-3a6 6 0 0 1 12 0v3"/>

                        <path d="M16 5a3 3 0 0 1 0 6"/>

                        <path d="M18 14a5 5 0 0 1 3 5v2"/>

                    </svg>

                    Nhóm sinh viên
                </a>
                </c:if>


                <%-- ================================= --%>
                <%-- HOI DONG BAO VE - TV4             --%>
                <%-- ================================= --%>

                <a class="nav-link"
                   href="<c:url value='/admin/councils/assignments'/>"
                   aria-current="${fn:contains(currentPath, '/admin/councils') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                        <circle cx="9" cy="7" r="4"/>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>

                    </svg>

                    Phân công đánh giá
                </a>

            </c:if>


            <%-- ========================================= --%>
            <%-- TV3: MENU RIENG CHO TRUONG KHOA         --%>
            <%-- DEAN XET DUYET DANG KY DE TAI CUA NHOM  --%>
            <%-- ========================================= --%>

            <c:if test="${pageContext.request.isUserInRole('DEAN')}">

                <a class="nav-link"
                   href="<c:url value='/dean/group-topic-registrations'/>"
                   aria-current="${fn:contains(currentPath, '/dean/group-topic-registrations') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <path d="M9 11l3 3L22 4"/>

                        <path d="M21 12v7a2 2 0 0 1-2 2H5
                                 a2 2 0 0 1-2-2V5
                                 a2 2 0 0 1 2-2h11"/>

                    </svg>

                    Xét duyệt đăng ký đề tài
                </a>


                <a class="nav-link"
                   href="<c:url value='/dean/results'/>"
                   aria-current="${fn:contains(currentPath, '/dean/results') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                        <line x1="16" y1="13" x2="8" y2="13"/>
                        <line x1="16" y1="17" x2="8" y2="17"/>
                        <polyline points="10 9 9 9 8 9"/>

                    </svg>

                    Công bố kết quả
                </a>

            </c:if>


            <%-- ========================================= --%>
            <%-- TV3: GIANG VIEN QUAN LY NHOM SINH VIEN --%>
            <%-- KHONG HIEN THI CAC MENU ADMIN KHAC     --%>
            <%-- ========================================= --%>

            <c:if test="${pageContext.request.isUserInRole('LECTURER')}">

                <a class="nav-link"
                   href="<c:url value='/admin/student-groups'/>"
                   aria-current="${fn:contains(currentPath, '/admin/student-groups') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <circle cx="9" cy="8" r="3"/>
                        <path d="M3 21v-3a6 6 0 0 1 12 0v3"/>
                        <path d="M16 5a3 3 0 0 1 0 6"/>
                        <path d="M18 14a5 5 0 0 1 3 5v2"/>

                    </svg>

                    Nhóm sinh viên
                </a>

                <a class="nav-link"
                   href="<c:url value='/lecturer/councils'/>"
                   aria-current="${fn:contains(currentPath, '/lecturer/councils') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                        <circle cx="9" cy="7" r="4"/>

                    </svg>

                    Hội đồng của tôi
                </a>

                <a class="nav-link"
                   href="<c:url value='/lecturer/evaluations'/>"
                   aria-current="${fn:contains(currentPath, '/lecturer/evaluations') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>

                    </svg>

                    Chấm điểm đề tài
                </a>

            </c:if>


            <%-- ========================================= --%>
            <%-- TV3: MENU BAO CAO SINH VIEN             --%>
            <%-- DANH CHO DEAN VA LECTURER               --%>
            <%-- ========================================= --%>

            <c:if test="${pageContext.request.isUserInRole('DEAN')
                         or pageContext.request.isUserInRole('LECTURER')}">

                <a class="nav-link"
                   href="<c:url value='/review/submissions'/>"
                   aria-current="${fn:contains(currentPath, '/review/submissions') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <path d="M6 3h9l5 5v13H6z"/>

                        <path d="M14 3v6h6"/>

                        <path d="M9 13h8"/>

                        <path d="M9 17h8"/>

                    </svg>

                    Báo cáo sinh viên
                </a>

            </c:if>


            <%-- ========================================= --%>
            <%-- TV3: MENU RIENG CHO SINH VIEN           --%>
            <%-- ========================================= --%>

            <c:if test="${pageContext.request.isUserInRole('STUDENT')}">

                <a class="nav-link"
                   href="<c:url value='/student/my-groups'/>"
                   aria-current="${fn:contains(currentPath, '/student/my-groups') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <circle cx="9" cy="8" r="3"/>

                        <path d="M3 21v-3a6 6 0 0 1 12 0v3"/>

                        <path d="M16 5a3 3 0 0 1 0 6"/>

                        <path d="M18 14a5 5 0 0 1 3 5v2"/>

                    </svg>

                    Nhóm của tôi
                </a>

                <a class="nav-link"
                   href="<c:url value='/student/my-results'/>"
                   aria-current="${fn:contains(currentPath, '/student/my-results') ? 'page' : 'false'}">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24"
                         aria-hidden="true">

                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>

                    </svg>

                    Kết quả đề tài
                </a>

            </c:if>


            <%-- ========================================= --%>
            <%-- HO SO CA NHAN - MOI VAI TRO              --%>
            <%-- ========================================= --%>

            <a class="nav-link"
               href="<c:url value='/profile'/>"
               aria-current="${fn:contains(currentPath, '/profile') ? 'page' : 'false'}">

                <svg class="nav-icon"
                     viewBox="0 0 24 24"
                     aria-hidden="true">

                    <circle cx="12" cy="8" r="4"/>

                    <path d="M4 21v-2a8 8 0 0 1 16 0v2"/>

                </svg>

                Hồ sơ cá nhân
            </a>

        <c:if test="${pageContext.request.isUserInRole('LECTURER')}">
            <a class="nav-link" href="<c:url value='/admin/registration-periods'/>">Đợt môn/lớp phụ trách</a>
        </c:if>
            <c:if test="${pageContext.request.isUserInRole('ADMIN') or pageContext.request.isUserInRole('DEAN') or pageContext.request.isUserInRole('LECTURER')}">
                <a class="nav-link" href="<c:url value='/course-registrations'/>" aria-current="${fn:contains(currentPath, '/course-registrations') ? 'page' : 'false'}">
                    <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 5h7l2 2h9v14H3z"/></svg>Môn/lớp đăng ký
                </a>
            </c:if>
        </nav>


        <%-- ========================================= --%>
        <%-- SIDEBAR BOTTOM                           --%>
        <%-- ========================================= --%>

        <div class="sidebar-bottom">

            <p class="sidebar-note">
                Cổng quản lý đề tài<br>
                Dành cho giảng viên và sinh viên
            </p>


            <%-- DANG XUAT --%>

            <form action="<c:url value='/logout'/>"
                  method="post">

                <input type="hidden"
                       name="${_csrf.parameterName}"
                       value="${_csrf.token}">

                <button type="submit"
                        class="logout-button">
                    <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M9 4H4v16h5M14 8l4 4-4 4M8 12h10"/>
                    </svg>
                    <span>Đăng xuất</span>
                </button>

            </form>

        </div>

    </aside>


    <%-- ========================================= --%>
    <%-- MAIN WORKSPACE                            --%>
    <%-- ========================================= --%>

    <div class="workspace">


        <%-- TOPBAR --%>

        <header class="topbar">


            <%-- BREADCRUMB --%>

            <div class="breadcrumb">

                <a href="<c:url value='/'/>">
                    Không gian làm việc
                </a>

                <span>/</span>

                <c:out value="${pageTitle}"/>

            </div>


            <%-- THONG TIN TAI KHOAN --%>

            <a class="account-link"
               href="<c:url value='/profile'/>">

                <span class="avatar"
                      aria-hidden="true">

                    <svg class="nav-icon"
                         viewBox="0 0 24 24">

                        <circle cx="12" cy="8" r="4"/>

                        <path d="M4 21a8 8 0 0 1 16 0"/>

                    </svg>

                </span>

                <c:out value="${pageContext.request.userPrincipal.principal.fullName}"/>

            </a>

        </header>


        <%-- ========================================= --%>
        <%-- MAIN CONTENT                              --%>
        <%-- ========================================= --%>

        <main class="container" id="main-content">


            <%-- THONG BAO THANH CONG --%>

            <c:if test="${not empty success}">

                <div class="alert success"
                     role="status">

                    <c:out value="${success}"/>

                </div>

            </c:if>


            <%-- THONG BAO LOI --%>

            <c:if test="${not empty error}">

                <div class="alert error"
                     role="alert">

                    <c:out value="${error}"/>

                </div>

            </c:if>


            <%-- Noi dung cua tung trang JSP bat dau tai day --%>
