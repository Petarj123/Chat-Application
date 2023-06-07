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

  void _sendRecoveryEmail() async {
    if (_formKey.currentState!.validate()) {
      final email = _emailController.text;

      try {
        final response = await http.post(
          Uri.parse('http://localhost:8080/api/auth/recovery'),
          headers: <String, String>{
            'Content-Type': 'application/json; charset=UTF-8',
          },
          body: jsonEncode(<String, String>{
            'email': email,
          }),
        );

        if (response.statusCode == 200) {
          // Recovery email sent successfully, navigate to the login page
          Navigator.pushReplacementNamed(context, '/login');
        } else {
          // Handle API error
          print('Recovery email failed. Status code: ${response.statusCode}');
        }
      } catch (e) {
        // Handle API call exception
        print('Recovery email failed. Error: $e');
      }
    }
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          child: Container(
            width: 300,
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Form(
                key: _formKey,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    Text(
                      'Password Recovery',
                      style: Theme.of(context).textTheme.headline4,
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
                      onPressed: _sendRecoveryEmail,
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
