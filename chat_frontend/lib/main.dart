import 'package:chat_frontend/widgets/ChatWidget.dart';
import 'package:chat_frontend/widgets/PasswordRecoveryWidget.dart';
import 'package:chat_frontend/widgets/PasswordResetWidget.dart';
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
      initialRoute: '/',
      onGenerateRoute: (RouteSettings settings) {
        WidgetBuilder builder;
        // Parse the incoming route
        final Uri uri = Uri.parse(settings.name!);

        // Handle specific routes
        switch (uri.path) {
          case '/login':
            builder = (BuildContext _) => LoginWidget();
            break;
          case '/register':
            builder = (BuildContext _) => RegisterWidget();
            break;
          case '/password-recovery':
            builder = (BuildContext _) => PasswordRecoveryWidget();
            break;
          case '/reset-password':
            final String token = uri.queryParameters['token'] ?? '';
            builder = (BuildContext _) => PasswordResetWidget(token: token);
            break;
          case '/chat':
            builder = (BuildContext _) => ChatWidget();
          default:
          // Redirect to login page as a fallback
            builder = (BuildContext _) => LoginWidget();
            break;
        }

        return MaterialPageRoute(builder: builder, settings: settings);
      },
    );
  }
}
