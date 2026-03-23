package org.delcom.pam_proyek1_ifs23049.helper

class ConstHelper {
    enum class RouteNames(val path: String) {
        AuthLogin(path = "auth/login"),
        AuthRegister(path = "auth/register"),

        Home(path = "home"),
        Profile(path = "profile"),

        Books(path = "books"),
        BooksAdd(path = "books/add"),
        BooksDetail(path = "books/{bookId}"),
        BooksEdit(path = "books/{bookId}/edit"),
    }
}