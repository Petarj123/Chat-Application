import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
class PasswordRecoveryWidget extends StatefulWidget {
  @override
  _PasswordRecoveryWidgetState createState() => _PasswordRecoveryWidgetState();
}
class _PasswordRecoveryWidgetState extends State<PasswordRecoveryWidget> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _emailController = TextEditingController();
  Future<bool> _sendRecoveryEmail(String email) async {
    try {
      final response = await http.post(
        Uri.parse('http://192.168.0.18:8080/api/auth/recovery'),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(<String, String>{
          'email': email,
        }),
      );
      if (response.statusCode == 200) {
        return true;
      } else {
        print('Recovery email failed. Status code: ${response.statusCode}');
        return false;
      }
    } catch (e) {
      print('Recovery email failed. Error: $e');
      return false;
    }
  }
  void _handleRecoveryEmail() async {
    if (_formKey.currentState!.validate()) {
      final email = _emailController.text;
      final success = await _sendRecoveryEmail(email);
      if (success) {
        // Recovery email sent successfully, navigate to the login page
        Navigator.pushReplacementNamed(context, '/login');
      }
    }
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          child: SizedBox(
            width: 300,
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Form(
                key: _formKey,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    const Text(
                      'Password Recovery',
                      style: TextStyle(fontSize: 24),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 32),
                    TextFormField(
                      controller: _emailController,
                      decoration: const InputDecoration(
                        labelText: 'Email',
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter your email';
                        }
                        // Add email validation logic if needed
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _handleRecoveryEmail,
                      child: const Text('Send Recovery Email'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}