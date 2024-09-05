# Data Mesh
## Apache Beam
The unified program model for defining both batch and streaming data-parallel processing pipelines as well as a set of language-specific SDKs for constructing pipelines and Runners for executing them on distributed processing backends, including Apache Flink, Apache Spark, Google Cloud Dataflow and Hazelcast Jet.
GitHub site: https://github.com/apache/beam
Official web site: https://beam.apache.org/

## Project Review Model 
GRAI | PDCA | SMART
---|---|---
Goal/回顾目标: 写下最初定下的目标<br>- 回顾why：当初为什么做这件事？<br>- 回顾what：要达成的目标是什么？关键结果是什么？| Plan/计划: 定计划，排优先级，资源调配，预判结果<br>- Who will complete what when<br>- Goal, what's resources, what's result | Specific/明确性:<br> 具体的语言清楚地说明要达成的行为标准，目标要清晰，明确
Result/评估结果: 用结果和目标做对比<br>- 结果在哪个层级？<br>- 哪些超出了预期？<br>- 哪些做的还不够？| Do/执行: 执行计划<br>- List efficiency steps and methods that can complete the plan<br>- Just do it! | Measurable/衡量性: 目标是明确的，作为衡量是否达成目标的依据
Analysis/分析原因: 根据结果分析原因<br>- 做成了是因为什么？<br>- 为什么没做好？ | Check/检查结果: 检查结果<br>- Check every things that are assigned already | Attainable/可实现性:<br> 可通过努力实现的，不能过高或过低
Insight/总结规律: 总结经验和规律<br>- 一开始要注意什么？<br>- 哪些事应该避免？<br>- 遇到什么情况应止损？ | Action/处理: 总结问题，新问题<br>- Success experiences should have a process and reusable<br>- New problem must transfer to next iteration | Relevant/相关性:<br> 各项目之间有关联，相互支持，符合实际
||| Time-based/时限性:<br> 根据任务的权重、事情的轻重缓急，拟定出完成目标项目的时间要求，<br>定期检查项目的完成进度，及时掌握项目进展的变化情况，<br>以方便对下属进行及时的工作指导，以及根据工作计划的异常情况变化及时地调整工作计划

# Resume
## Basic Information
|||
| :---  | ---: |
Name:       Xin Yu Pan &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;   | &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Cell Phone: 18500852935
Sex:        Male | Email: pxy0592@msn.com

---
## Companies
- 国际商业机器（中国）投资有限公司 [ 2012.04 - present ]
- 北京天启太和软件有限公司 [ 2011.12 - 2012.03 ]
- 北京领云启创软件有限公司 [ 2004.07 - 2011.11 ]

---
## Skills
**Technical Stack**: Microservice, API Key Management, Graph Engine, ElasticSearch, RabbitMQ, Spark, Hadoop, DevOps, Kubernetes, OpenShift,  CI/CD, IBM Cloud
**Technical Role**: Backend Developer, Feature-level Micro-Architecture, Team Leader
**Program Languages**: Java, JUnit, JanusGraph, SPARQL, Neo4j/Cypher, Python, Operator, Ansible, Helm Chart, Shell, Jenkins  
**Soft Skills**: English communication, Time, Risk, Multiple-tasks management, Agile/Scrum, DevOps, SDLC

---
### Data Product Hub 5.1.0 release 
**Release Cycle**: 2024.08 - present  
    - Two weeks per sprint, 3 or 4 sprints avg
**Summary**: Lead small Indian team to deliver 2 tasks subsequently in two sprints  
**Project List**:  
- Pre-approved subscription policy for the data product based on the user list or the user groups (100% done)
  Extend the current subscription policy to support auto approval access control for the data product if the data product subscriber is in the user list or user groups. The number of users has supported more than 100,000 levels of enterprise customers
- Time-bound subscription policy for the data product (100% done)
  Extend the current subscription policy to support time-bound subscription policy. This is a fine-tune subscription policy that the data product provider can define a time window for the restricted data product for subscribers
- Comprehensive data management policy (50% done)
  Extend the current policy and also reduce the gap with the competitors that the data product providers have ability to define the data management policy with a set of rule-based access conditions 

---
### Data Product Hub 5.0.1/2/3 release 
**Release Cycle**: 2024.05 - 2024.07  
    - Two weeks per sprint, 2 sprints avg)  
**Summary**: As individual developer delivered 10+ small tasks and amount of defects fixing in parallel
**Project List**:  
- Security enhancement
  Enhance current product security holes that have been identified in retrospective phase. I'm responsible for the feature goal design, execution plan/tasks, regular progress update, coding/testing/deployment, and feature demo in team.  
  10 sub-tasks were completed in 4 sprints, in the meantime, delivered amount of defects fixing.

---
### Data Product Hub 5.0.0 release  
**Release Cycle**: 2024.03 - 2024.05  
    - Two weeks per sprint, 4 ~ 5 sprints in total)
**Summary**: As new product onboard to Cloud Pack for Data platform, comply with the mandatory requirements that CP4D required before official release  
**Project List**:  
- Mutual SSL support
- 

---
### IBM Global Search Service 2019.06 to 2024.02    (two weeks per sprint, 2 sprints per minor release/4 sprints per major release in parallel)
**Release Cycle**: 2019.06 - 2024.02
    - Two weeks per sprint, 2 sprints per minor release and 4 sprints per major release  
**Summary**:  
**Project List**:
- Keyword search
  Using Elasticsearch as search engine, developing a custom `gs_user_query` query clause improved matching and ranking through the use of query expansion and boosting
- Semantic search
  Based on knowledge graph, developing a light graph engine populates the metadata information from the knowledge graph to global search service. Technically using JanusGraph API implemented the query engine and also evaluated the other commercial graph database, such as Neo4J, delivered a serial of sessions to senior management and architectures
- Expand the semantic search capability to add common knowledge sense
  Leverage a model called `bio-inspired memories semantic model` from IBM Research to expand the semantic search capability, adding common knowledge sense for the user query

---
### IBM High Distributed Performance Cluster 2012.01 to 2019.05     (Waterfall Model, 3 ~ 4 months per major release)
**Release Cycle**: 2012.01 - 2019.05
    - Traditional waterfall model, 3 ~ 4 months per major release
**Summary**: 
- Lead 6 ~ 7 persons team focus on feature testing, system testing, performance testing, roadmap test release  
- Reliable and accountable delivered result in every major release
- 


---
### Education
Northeastern University Automation Control BS  
