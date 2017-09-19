## [0.136.0](https://github.com/hmrc/self-assessment-api/releases/tag/v0.136.0) 15-Sept-2017

* New errors responses are being returned after implementation of BVR's
  * MISALIGNED_PERIOD - period being submitted/created is not within the accounting period
  * NOT_CONTIGUOUS_PERIOD - period being submitted/created is not contiguous with the previous period
  * OVERLAPPING_PERIOD - period being submitted/created overlaps with any of the previously submitted periods
* Impacted API's
  * Create a self-employment update period
  * Create a non-FHL UK property update period
  * Create a FHL UK property update period
