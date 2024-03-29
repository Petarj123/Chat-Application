import 'package:flutter/material.dart';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
const String apiUrl = 'http://192.168.0.18:8080/api/auth/authenticate';
class LoginWidget extends StatefulWidget {
  LoginWidget({Key? key}) : super(key: key);
  @override
  _LoginWidgetState createState() => _LoginWidgetState();
}
class _LoginWidgetState extends State<LoginWidget> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  void _authenticate() async {
    final String email = _emailController.text;
    final String password = _passwordController.text;
    try {
      final http.Response response = await http.post(
        Uri.parse(apiUrl),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(<String, String>{
          'usernameOrEmail': email,
          'password': password,
        }),
      );
      if (response.statusCode == 200) {
        // Parse the response body for the token
        Map<String, dynamic> responseBody = jsonDecode(response.body);
        String token = responseBody['token'];
        // Save the token in local storage
        SharedPreferences prefs = await SharedPreferences.getInstance();
        await prefs.setString('token', token);
        Navigator.pushReplacementNamed(context, '/chat');
        print('Logged in successfully');
      } else {
        print('Failed to login');
      }
    } catch (e) {
      print('Error occurred during authentication: $e');
    }
  }
  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
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
                    Text(
                      'Login',
                      style: Theme.of(context).textTheme.headlineMedium,
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
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _passwordController,
                      decoration: const InputDecoration(
                        labelText: 'Password',
                        border: OutlineInputBorder(),
                      ),
                      obscureText: true,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter your password';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () {
                        if (_formKey.currentState!.validate()) {
                          _authenticate();
                        }
                      },
                      child: const Text('Sign in'),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: <Widget>[
                        TextButton(
                          onPressed: () {
                            Navigator.pushNamed(context, '/register');
                          },
                          child: const Text('Register'),
                        ),
                        TextButton(
                          onPressed: () {
                            Navigator.pushNamed(context, '/password-recovery');
                          },
                          child: const Text('Forgot password?'),
                        ),
                      ],
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