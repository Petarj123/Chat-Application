import 'package:chat_frontend/widgets/PasswordRecoveryWidget.dart';
import 'package:chat_frontend/widgets/RegisterWidget.dart';
import 'package:flutter/material.dart';
import 'package:chat_frontend/widgets/LoginWidget.dart';
void main() {
  runApp(MyApp());
}
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      initialRoute: '/login',
      routes: {
        '/login': (context) => LoginWidget(),
        '/register': (context) => RegisterWidget(),
        '/password-recovery': (context) => PasswordRecoveryWidget(),
      },
    );
  }
}
