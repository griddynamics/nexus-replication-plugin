# Nexus replication plugin

As Nexus Admin I'd like to keep data (artifacts) even if Nexus was destroyed.
For this I can setup 2 or more Nexus instances and pass artifacts to all of
them when artifact is initially uploaded to one of them.

The plugin is going to be installed on both sending side that wants to
pass an artifact and receiving side that can accept the artifact.

## Documentation

[User documentation](documentation/user_doc.md)

[Contributor documentation](documentation/contributor_doc.md)

## Mechanism

Given artifact is uploaded to one of the Nexus instances:
- A request is sent to Nexus-Receiver with info about artifact and repo it was
 placed in
- Nexus-Receiver must have a Proxy Repository configured to proxy Nexus-Sender.
 After Nexus-Receiver gets the request it fetches target artifact by means of
 Proxy Repo configured to fetch changes from Nexus-Sender.

For this a REST service is implemented with path:
`service/local/artifact/maven/update`.

## To be done

- Plugin can be configured using Nexus Capabilities
- In configuration it's possible to set:
    - Nexus-Receiver addresses and credentials to be authorized
    - A flag whether an integration for particular Nexus-Receiver is enabled.
 Nexus-Sender should not send updates to such integration, but must store
 them for future replay.
- Multiple Nexus-Receivers may be configured
- A queue of artifacts must form on Nexus-Sender side if some of
 Nexus-Receivers are not available (requests fail) or Nexus-Receivers are
 disabled in config file. The queue has to be flushed into a persistent storage
 (file) to keep it even if Nexus-Sender crashes.
- There must be a limit for a queue length. Default is 100K, but would be nice
 to configure it.
- Nexus performance in general and UI in particular must not suffer from
 replication. This implies that all events must be processed in an async manner
 both in Nexus-Sender & Nexus-Receiver.
- Logs must show errors if replication doesn't happen due to Nexus-Receiver
 unavailability

## Nice to have

- An email to send warnings can be configured. Notifications must be sent when
 artifacts queue is 100, 500, 1000, 10K, 100K long.
 
### Copyright and License

Copyright 2015, Grid Dynamics International, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [Apache License, Version 2.0](LICENSE.txt)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
