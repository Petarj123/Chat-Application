import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class ChatWidget extends StatelessWidget {
  void logout() {

  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Chat'),
      ),
      body: Row(
        children: <Widget>[
          Expanded(
            flex: 1,
            child: Container(
              color: Colors.grey[200],
              child: Column(
                children: <Widget>[
                  Expanded(
                    child: ListView.builder(
                      itemCount: 20, // replace with your data
                      itemBuilder: (context, index) {
                        return ListTile(
                          title: Text('Chat Room ${index + 1}'),
                          onTap: () {
                            // Handle chat room selection
                          },
                        );
                      },
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Row(
                      children: <Widget>[
                        ElevatedButton(
                          onPressed: () {
                            // Handle chat creation
                          },
                          child: const Text('Create Chat'),
                        ),
                        const SizedBox(width: 8),
                        IconButton(
                          icon: const Icon(Icons.exit_to_app),
                          onPressed: () {
                            // Handle logout
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const VerticalDivider(width: 1),
          Expanded(
            flex: 3,
            child: Column(
              children: <Widget>[
                Expanded(
                  child: ListView.builder(
                    itemCount: 20, // replace with your data
                    itemBuilder: (context, index) {
                      return ListTile(
                        title: Text('Message ${index + 1}'),
                      );
                    },
                  ),
                ),
                Container(
                  padding: const EdgeInsets.all(8.0),
                  child: Row(
                    children: <Widget>[
                      const Expanded(
                        child: TextField(
                          decoration: InputDecoration(
                            labelText: 'Enter message',
                          ),
                        ),
                      ),
                      IconButton(
                        icon: Icon(Icons.send),
                        onPressed: () {
                          // Handle message sending
                        },
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
